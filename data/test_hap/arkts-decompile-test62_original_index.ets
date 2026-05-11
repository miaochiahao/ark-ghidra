// === Round 62: Abstract class hierarchy, fluent builder,
//   deep clone, throttler, object pool ===

// --- Abstract class hierarchy ---

abstract class BaseShape {
  abstract getArea(): number;
  abstract getPerimeter(): number;

  describe(): string {
    return "area=" + String(this.getArea()) + ",perimeter=" + String(this.getPerimeter());
  }
}

class MyCircle extends BaseShape {
  private radius: number = 0;

  constructor(radius: number) {
    super();
    this.radius = radius;
  }

  getArea(): number {
    return this.radius * this.radius * 3;
  }

  getPerimeter(): number {
    return 2 * this.radius * 3;
  }
}

class MyRect extends BaseShape {
  private w: number = 0;
  private h: number = 0;

  constructor(w: number, h: number) {
    super();
    this.w = w;
    this.h = h;
  }

  getArea(): number {
    return this.w * this.h;
  }

  getPerimeter(): number {
    return 2 * (this.w + this.h);
  }
}

function testShapes(): string {
  let c: MyCircle = new MyCircle(5);
  let r: MyRect = new MyRect(4, 6);
  return c.describe() + "|" + r.describe();
}

// --- Fluent builder ---

class QueryBuilder {
  private tableName: string = '';
  private conditions: string[] = [];
  private orderField: string = '';
  private limitValue: number = 0;

  from(table: string): QueryBuilder {
    this.tableName = table;
    return this;
  }

  where(condition: string): QueryBuilder {
    this.conditions.push(condition);
    return this;
  }

  orderBy(field: string): QueryBuilder {
    this.orderField = field;
    return this;
  }

  limit(n: number): QueryBuilder {
    this.limitValue = n;
    return this;
  }

  build(): string {
    let result: string = "SELECT * FROM " + this.tableName;
    if (this.conditions.length > 0) {
      result = result + " WHERE " + this.conditions.join(" AND ");
    }
    if (this.orderField.length > 0) {
      result = result + " ORDER BY " + this.orderField;
    }
    if (this.limitValue > 0) {
      result = result + " LIMIT " + String(this.limitValue);
    }
    return result;
  }
}

function testQueryBuilder(): string {
  let query: string = new QueryBuilder()
    .from("users")
    .where("age > 18")
    .where("active = true")
    .orderBy("name")
    .limit(10)
    .build();
  return query;
}

// --- Deep clone (manual) ---

interface CloneableItem {
  name: string;
  value: number;
  tags: string[];
}

function deepClone(item: CloneableItem): CloneableItem {
  let clonedTags: string[] = [];
  for (let i: number = 0; i < item.tags.length; i++) {
    clonedTags.push(item.tags[i]);
  }
  let result: CloneableItem = { name: item.name, value: item.value, tags: clonedTags };
  return result;
}

function testDeepClone(): string {
  let original: CloneableItem = { name: "test", value: 42, tags: ["a", "b", "c"] };
  let cloned: CloneableItem = deepClone(original);
  cloned.tags.push("d");
  return original.tags.length + "," + cloned.tags.length;
}

// --- Throttler ---

class Throttler {
  private lastCall: number = 0;
  private interval: number = 0;
  private callCount: number = 0;

  constructor(interval: number) {
    this.interval = interval;
    this.lastCall = 0;
    this.callCount = 0;
  }

  tryCall(now: number): boolean {
    if (now - this.lastCall >= this.interval) {
      this.lastCall = now;
      this.callCount++;
      return true;
    }
    return false;
  }

  getCallCount(): number {
    return this.callCount;
  }
}

function testThrottler(): string {
  let t: Throttler = new Throttler(100);
  t.tryCall(50);
  t.tryCall(120);
  t.tryCall(150);
  t.tryCall(250);
  return String(t.getCallCount());
}

// --- Object pool ---

class PoolableObject {
  value: number = 0;
  active: boolean = false;
}

class ObjectPool {
  private available: PoolableObject[] = [];
  private inUse: PoolableObject[] = [];
  private created: number = 0;

  acquire(): PoolableObject {
    if (this.available.length > 0) {
      let obj: PoolableObject = this.available.pop()!;
      obj.active = true;
      this.inUse.push(obj);
      return obj;
    }
    let obj: PoolableObject = new PoolableObject();
    obj.active = true;
    this.created++;
    this.inUse.push(obj);
    return obj;
  }

  release(obj: PoolableObject): void {
    obj.active = false;
    obj.value = 0;
    let idx: number = -1;
    for (let i: number = 0; i < this.inUse.length; i++) {
      if (this.inUse[i] === obj) {
        idx = i;
      }
    }
    if (idx >= 0) {
      this.inUse.splice(idx, 1);
      this.available.push(obj);
    }
  }

  getStats(): string {
    return String(this.created) + "," + String(this.available.length) + "," + String(this.inUse.length);
  }
}

function testObjectPool(): string {
  let pool: ObjectPool = new ObjectPool();
  let a: PoolableObject = pool.acquire();
  let b: PoolableObject = pool.acquire();
  pool.release(a);
  let c: PoolableObject = pool.acquire();
  return pool.getStats();
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest62';

  build() {
    Column() {
      Text(this.message)
        .fontSize(30)
        .fontWeight(FontWeight.Bold)
    }
    .width('100%')
    .height('100%')
  }
}
