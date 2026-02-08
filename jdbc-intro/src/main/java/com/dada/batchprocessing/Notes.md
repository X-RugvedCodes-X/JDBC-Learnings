# Batch Processing in JDBC

Batch processing with prepared statements in JDBC is about sending **many similar SQL operations to the database in groups** instead of one-by-one.
Done right, it gives you **huge performance gains**, cleaner code, and safer SQL.

---

## What problem batching solves

If you insert 10,000 rows like this:

```java
for (...) {
    Statement st = conn.createStatement();
    st.executeUpdate("insert into user values (...)");
}
```

you pay the cost of:

- parsing SQL 10,000 times
- network round-trips 10,000 times
- transaction overhead 10,000 times

That’s slow.

With batching, you prepare **once**, send many parameter sets, and the driver ships them efficiently.

---

## Why PreparedStatement (not Statement)

Prepared statements:

- prevent SQL injection
- are precompiled / cached by DB
- handle types correctly
- are faster for repeated execution
- work naturally with batching

---

## Core idea

1. Create a `PreparedStatement` with placeholders.
2. Set parameters for each record.
3. Add to batch.
4. Execute the batch periodically.
5. Commit.

---

## Canonical pattern (gold standard)

```java
String sql = "INSERT INTO users(id, name, email) VALUES (?, ?, ?)";

try (Connection con = dataSource.getConnection();
     PreparedStatement ps = con.prepareStatement(sql)) {

    con.setAutoCommit(false);   // IMPORTANT

    int batchSize = 1000;
    int count = 0;

    for (User u : users) {
        ps.setInt(1, u.getId());
        ps.setString(2, u.getName());
        ps.setString(3, u.getEmail());

        ps.addBatch();
        count++;

        if (count % batchSize == 0) {
            ps.executeBatch();
            ps.clearBatch();   // good hygiene
        }
    }

    ps.executeBatch();  // leftovers
    con.commit();
}
```

---

# Best Practices (the ones seniors expect)

## 1) Disable auto-commit

If you don’t, every statement becomes its own transaction → destroys performance.

```java
con.setAutoCommit(false);
```

Commit once per batch (or logical unit).

---

## 2) Use a sensible batch size

Too small → no benefit
Too large → memory issues / packet overflow

Typical values:

- 500
- 1000
- sometimes 5000 (depends on driver & row size)

There is no universal number — measure.

---

## 3) Execute periodically, not only at the end

Avoids:

- OutOfMemoryError
- giant rollback if something fails

---

## 4) Always handle leftovers

After loop, run one final `executeBatch()`.

People forget this constantly.

---

## 5) Use try-with-resources

Guarantees closure even on exceptions.

---

## 6) Clear the batch after execution

Prevents re-sending the same commands accidentally.

```java
ps.clearBatch();
```

---

## 7) Proper exception handling + rollback

```java
try {
    ps.executeBatch();
    con.commit();
} catch (SQLException e) {
    con.rollback();
    throw e;
}
```

---

## 8) Prefer reusing the PreparedStatement

Do NOT recreate inside the loop.

Bad 

```java
for (...) {
   PreparedStatement ps = con.prepareStatement(sql);
}
```

---

## 9) Watch out for driver optimizations

Some drivers (MySQL, PostgreSQL) need flags like:

```
rewriteBatchedStatements=true
```

to turn batches into multi-value inserts internally → massive speedup.

---

## 10) Consider generated keys carefully

If you ask for generated keys, batching can slow down or behave differently.

---

---

# How much faster is batching?

Real systems often see:

**10x – 100x improvement**

Especially when network latency is high.

---


# Advanced patterns professionals use

## Batch updates

```java
String sql = "UPDATE users SET status=? WHERE id=?";
```

Same approach.

---

## Mixed success handling

`executeBatch()` returns an `int[]` telling which statements succeeded.

Useful for partial retry logic.

---

## Streaming large imports

Read file → map → batch → commit → repeat.

Prevents memory explosion.

---


# Common mistakes juniors make

- Forgetting `setAutoCommit(false)`
- Not committing
- Huge batches
- Creating statement inside loop
- Forgetting final execute
- Ignoring rollback
- Mixing business logic inside DB loop

---


## Mental model to remember

PreparedStatement = **template**
Batch = **stack many filled templates → send together**

---


# Most Common Asked Questions

Perfect. These are exactly the questions that turn a “knows JDBC” candidate into an **engineer**.

I’ll answer them the way strong interview answers usually sound: practical, trade-off aware, and production minded.

---

# 1) What if a batch partially fails?

In JDBC, `executeBatch()` returns an `int[]`.

Each element corresponds to one statement.

Possible values:

* `>= 0` → success, rows affected
* `Statement.SUCCESS_NO_INFO` → success but unknown count
* `Statement.EXECUTE_FAILED` → failed

---

### Interview-quality answer

If auto-commit is **false** (like in your code), I have two options:

### Option A – Fail whole transaction (most common)

Rollback everything → keep data consistent.

Used when:
✔ atomicity is important
✔ partial data is dangerous

```
if any failed → rollback()
```

---

### Option B – Accept partial success

Commit successful ones, track failures.

Used in:
✔ bulk ingestion
✔ logs
✔ analytics

---

### Bonus senior point 

Some drivers stop execution at first error.
So you might not even know later results.

---

---

# 2) How would you retry only failed rows?

After `executeBatch()`:

```
for each index i:
    if result[i] == EXECUTE_FAILED
        retry that record
```

Usually retried:

* individually
* or in a smaller batch

---

### But production systems also:

✔ limit retry attempts
✔ log permanently failing rows
✔ move them to a **dead-letter table/file**

Otherwise infinite loop 

---

### Example strategy

Attempt 1 → batch
Attempt 2 → retry failed individually
Still fail → log + skip

---

---

# 3) What if 10 million records?

If a candidate says:

> I’ll store them in a List and then batch

Immediate rejection 
→ memory explosion.

---

### Correct approach

**Stream / chunk processing**.

Read small portions → batch → commit → release memory.

Example:

```
read 10k
insert batch
commit
clear
repeat
```

---

### Bonus senior point 

You may also:

* disable indexes temporarily
* tune commit frequency
* use database bulk loaders (COPY, LOAD DATA)

---

---

# 4) Memory considerations?

Main memory consumers:

* objects in batch
* driver buffers
* result arrays

---

### Best practices

✔ small batch sizes
✔ clearBatch()
✔ avoid large collections
✔ commit periodically

---

If memory spikes → GC pressure → slow system.

---


# 5) When does batching become slower?

Fun question. Interviewers love it.

People assume bigger batch = faster.

Not always.

---

### Too large → problems

* network packet too big
* DB lock contention
* transaction log heavy
* memory overhead
* long rollback time if failure

---

### Real answer

We **benchmark** and find sweet spot.

Often:

```
100 – 1000
```

depends on:
driver, DB, network.

---

---

# 6) How would you design this in layers?

Huge signal of maturity.

---

### Instead of everything in `main()`:

```
Controller / UI
↓
Service (business logic)
↓
Repository / DAO (JDBC)
↓
Database
```

---

### Why?

✔ testability
✔ replace DB easily
✔ easier maintenance
✔ separation of concerns

---

### Interview gold line 

> UI should not know how persistence works.

---

---

# 7) How would this run in production?

Not from console input 

Data might come from:

* files
* APIs
* queues
* scheduled jobs

---

### You would add

✔ connection pool (HikariCP)
✔ logging
✔ config via env
✔ exception strategy
✔ graceful shutdown
✔ retries
✔ metrics

---

### Another strong sentence 

> Production systems must be observable and recoverable.

---

# 8) How would you monitor it?

You track:

✔ number of processed rows
✔ failures
✔ retries
✔ processing time
✔ memory / CPU

---

Tools:

* logs
* metrics (Prometheus)
* dashboards
* alerts

---

Example:

```
Processed: 1,000,000
Failed: 12
Retry: 10 success
2 moved to DLQ
```
---