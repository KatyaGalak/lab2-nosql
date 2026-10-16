package com.university.booking.service;

import com.university.booking.dto.BookingRequest;
import com.university.booking.enums.BookingStatus;
import com.university.booking.enums.RedisKeyPrefix;
import com.university.booking.exception.ConflictException;
import com.university.booking.exception.ResourceNotFoundException;
import com.university.booking.exception.ServiceUnavailableException;
import com.university.booking.exception.ValidationException;
import com.university.booking.model.Booking;
import com.university.booking.model.Cart;
import com.university.booking.model.CartItem;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private static final RedisScript<Long> DELETE_IF_UNCHANGED = RedisScript.of("""
            if redis.call('HLEN', KEYS[1]) ~= #ARGV then
                return 0
            end
            for i = 1, #ARGV do
                if redis.call('HEXISTS', KEYS[1], ARGV[i]) == 0 then
                    return 0
                end
            end
            return redis.call('DEL', KEYS[1])
            """, Long.class);

    private final RedisTemplate<String, Object> redisTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final BookingService bookingService;

    @Value("${app.cart.ttl-seconds:900}")
    private long cartTtlSeconds;

    public Cart addItem(String personId, BookingRequest request) {
        bookingService.validateBookingTarget(personId, request);

        String key = RedisKeyPrefix.CART.getKey(personId);
        CartItem item = CartItem.from(request);

        redis(() -> redisTemplate.execute(new SessionCallback<List<Object>>() {
            @Override
            public <K, V> List<Object> execute(@NonNull RedisOperations<K, V> operations) {
                RedisOperations<String, Object> ops = (RedisOperations<String, Object>) operations;
                ops.multi();
                ops.opsForHash().put(key, item.getId(), item);
                ops.expire(key, Duration.ofSeconds(cartTtlSeconds));
                return ops.exec();
            }
        }));

        return getCart(personId);
    }

    public Cart getCart(String personId) {
        String key = RedisKeyPrefix.CART.getKey(personId);
        Map<Object, Object> entries = redis(() -> redisTemplate.opsForHash().entries(key));
        Long ttl = redis(() -> redisTemplate.getExpire(key));

        List<CartItem> items = entries.values().stream()
                .map(CartItem.class::cast)
                .sorted(Comparator.comparing(CartItem::getAddedAt))
                .toList();

        return Cart.builder()
                .personId(personId)
                .items(items)
                .ttlSeconds(ttl != null && ttl > 0 ? ttl : 0)
                .build();
    }

    public Cart removeItem(String personId, String itemId) {
        Long removed = redis(() -> redisTemplate.opsForHash().delete(RedisKeyPrefix.CART.getKey(personId), itemId));
        if (removed == null || removed == 0) {
            throw new ResourceNotFoundException(itemId);
        }
        return getCart(personId);
    }

    public void clearCart(String personId) {
        redis(() -> redisTemplate.delete(RedisKeyPrefix.CART.getKey(personId)));
    }

    @Transactional
    public List<Booking> checkout(String personId) {
        Cart cart = getCart(personId);
        if (cart.getItems().isEmpty()) {
            throw new ValidationException("Корзина пуста или время её хранения истекло");
        }

        List<Booking> bookings = cart.getItems().stream()
                .map(item -> bookingService.createBooking(personId, item.toBookingRequest(), BookingStatus.UNDER_REVIEW))
                .toList();

        Object[] itemIds = cart.getItems().stream().map(CartItem::getId).toArray();
        Long deleted = redis(() -> stringRedisTemplate.execute(DELETE_IF_UNCHANGED,
                List.of(RedisKeyPrefix.CART.getKey(personId)), itemIds));
        if (deleted == null || deleted == 0) {
            throw new ConflictException("Корзина изменилась или истекла во время оформления, попробуйте ещё раз");
        }

        return bookings;
    }

    private <T> T redis(Supplier<T> action) {
        try {
            return action.get();
        } catch (DataAccessException e) {
            log.atWarn()
                    .addKeyValue("event", "cart_unavailable")
                    .addKeyValue("error", e.getClass().getSimpleName())
                    .log("redis unavailable, cart operation rejected: {}", e.getMessage());
            throw new ServiceUnavailableException("Корзина временно недоступна, попробуйте позже");
        }
    }
}
