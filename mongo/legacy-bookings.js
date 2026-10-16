try {
  db.bookings.insertOne({
    _id: "invalid-demo",
    personId: "100000", roomId: "L9-1", categoryId: "meeting", eventName: "Нарушает схему",
    eventDate: ISODate("2025-01-10"), startTime: "10:00:00", endTime: "11:00:00",
    participantCount: 2, status: "DRAFT", createdAt: new Date(),
    comments: [], history: [{ status: "DRAFT", changedAt: new Date() }], schemaVersion: 2
  });
  print("ОШИБКА: документ вставлен, валидатор не сработал");
} catch (e) {
  print("Валидатор отклонил документ: " + e.errInfo.details.schemaRulesNotSatisfied
    .map(r => r.operatorName).join(", "));
}

const legacy = [
  {
    _id: "legacy-no-history",
    personId: "100000", roomId: "L9-3", categoryId: "meeting", eventName: "Старая заявка без истории",
    eventDate: ISODate("2025-02-01"), startTime: "09:00:00", endTime: "10:00:00",
    participantCount: 3, contactPhone: "+79000000000", status: "REJECTED",
    processedBy: "100000", createdAt: ISODate("2025-01-20T10:00:00Z"), processedAt: ISODate("2025-01-21T10:00:00Z")
  },
  {
    _id: "legacy-v1-history",
    personId: "100000", organizationId: "pikt", roomId: "L9-4", categoryId: "lecture",
    eventName: "Заявка с историей первой версии",
    eventDate: ISODate("2025-03-01"), startTime: "12:00:00", endTime: "13:30:00",
    participantCount: 5, contactPhone: "+79000000000", status: "APPROVED",
    processedBy: "100000", createdAt: ISODate("2025-02-20T10:00:00Z"), processedAt: ISODate("2025-02-21T10:00:00Z"),
    comments: [],
    history: [
      { status: "DRAFT", changedAt: ISODate("2025-02-20T10:00:00Z") },
      { status: "UNDER_REVIEW", changedAt: ISODate("2025-02-20T11:00:00Z") },
      { status: "APPROVED", changedAt: ISODate("2025-02-21T10:00:00Z") }
    ],
    schemaVersion: 1
  }
];
db.bookings.deleteMany({ _id: { $in: legacy.map(d => d._id) } });
db.runCommand({ insert: "bookings", documents: legacy, bypassDocumentValidation: true });
print("Документов старой схемы: " + db.bookings.countDocuments({
  $or: [{ schemaVersion: { $exists: false } }, { schemaVersion: { $lt: 2 } }]
}));