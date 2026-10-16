const INDEX = "idx_person_status_created";
const filter = { personId: "200001", status: "UNDER_REVIEW" };
const sort = { createdAt: -1 };

function summary(title) {
  const e = db.bookings.find(filter).sort(sort).explain("executionStats");
  let plan = e.queryPlanner.winningPlan;
  if (plan.queryPlan) plan = plan.queryPlan;
  const stages = [];
  for (let s = plan; s; s = s.inputStage) stages.push(s.stage + (s.indexName ? "(" + s.indexName + ")" : ""));
  const st = e.executionStats;
  print(`\n${title}`);
  print(`  план:               ${stages.join(" <- ")}`);
  print(`  nReturned:          ${st.nReturned}`);
  print(`  totalKeysExamined:  ${st.totalKeysExamined}`);
  print(`  totalDocsExamined:  ${st.totalDocsExamined}`);
  print(`  executionTimeMillis:${st.executionTimeMillis}`);
}

print("Документов в bookings: " + db.bookings.estimatedDocumentCount());

const existing = db.bookings.getIndexes().find(i => i.name === INDEX);
if (existing) db.bookings.dropIndex(INDEX);
summary("БЕЗ составного индекса");

db.bookings.createIndex({ personId: 1, status: 1, createdAt: -1, _id: -1 }, { name: INDEX });
summary("С индексом { personId: 1, status: 1, createdAt: -1, _id: -1 }");
