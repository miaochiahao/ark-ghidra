// === Round 27: This-based fluent API returning this, chained builder,
//   class method override detection, static initializer patterns,
//   nested class simulation, default value via helper, abstract base
//   pattern, sealed class simulation (tagged union), defensive copy ===

// --- Fluent API returning this ---

class FluentCounter {
  private count: number;

  constructor() {
    this.count = 0;
  }

  public increment(): FluentCounter {
    this.count = this.count + 1;
    return this;
  }

  public add(n: number): FluentCounter {
    this.count = this.count + n;
    return this;
  }

  public reset(): FluentCounter {
    this.count = 0;
    return this;
  }

  public value(): number {
    return this.count;
  }
}

function testFluentCounter(): string {
  let c: FluentCounter = new FluentCounter();
  let v1: number = c.increment().increment().increment().value();
  let c2: FluentCounter = new FluentCounter();
  let v2: number = c2.add(10).add(5).value();
  return String(v1) + ',' + String(v2);
}

// --- Builder pattern with this return ---

class QueryBuilder {
  private table: string;
  private conditions: string[];
  private limitVal: number;

  constructor(table: string) {
    this.table = table;
    this.conditions = [];
    this.limitVal = 0;
  }

  public where(cond: string): QueryBuilder {
    this.conditions.push(cond);
    return this;
  }

  public limit(n: number): QueryBuilder {
    this.limitVal = n;
    return this;
  }

  public build(): string {
    let q: string = 'SELECT * FROM ' + this.table;
    if (this.conditions.length > 0) {
      q = q + ' WHERE ';
      for (let i: number = 0; i < this.conditions.length; i++) {
        if (i > 0) {
          q = q + ' AND ';
        }
        q = q + this.conditions[i];
      }
    }
    if (this.limitVal > 0) {
      q = q + ' LIMIT ' + String(this.limitVal);
    }
    return q;
  }
}

function testQueryBuilder(): string {
  let q: string = new QueryBuilder('users')
    .where('age > 18')
    .where('active = true')
    .limit(10)
    .build();
  return q;
}

// --- Tagged union (sealed class simulation) ---

type ShapeType = 'circle' | 'rect' | 'tri';

class TaggedShape {
  public tag: ShapeType;
  public r: number;
  public w: number;
  public h: number;
  public base: number;
  public triH: number;

  constructor() {
    this.tag = 'circle';
    this.r = 0;
    this.w = 0;
    this.h = 0;
    this.base = 0;
    this.triH = 0;
  }

  public static makeCircle(r: number): TaggedShape {
    let s: TaggedShape = new TaggedShape();
    s.tag = 'circle';
    s.r = r;
    return s;
  }

  public static makeRect(w: number, h: number): TaggedShape {
    let s: TaggedShape = new TaggedShape();
    s.tag = 'rect';
    s.w = w;
    s.h = h;
    return s;
  }

  public area(): number {
    if (this.tag === 'circle') {
      return 3 * this.r * this.r;
    }
    if (this.tag === 'rect') {
      return this.w * this.h;
    }
    return 0;
  }
}

function testTaggedUnion(): string {
  let c: TaggedShape = TaggedShape.makeCircle(5);
  let r: TaggedShape = TaggedShape.makeRect(3, 4);
  return String(c.area()) + ',' + String(r.area());
}

// --- Defensive copy pattern ---

class Point {
  public x: number;
  public y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public copy(): Point {
    return new Point(this.x, this.y);
  }

  public distanceTo(other: Point): number {
    let dx: number = this.x - other.x;
    let dy: number = this.y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }

  public translate(dx: number, dy: number): Point {
    this.x = this.x + dx;
    this.y = this.y + dy;
    return this;
  }
}

function testDefensiveCopy(): string {
  let p1: Point = new Point(0, 0);
  let p2: Point = p1.copy();
  p2.translate(3, 4);
  let dist: number = p1.distanceTo(p2);
  return String(dist) + ',' + String(p1.x) + ',' + String(p2.x);
}

// --- Static initializer simulation ---

class Config {
  private static version: number = 0;
  private static label: string = '';

  public static init(v: number, n: string): void {
    Config.version = v;
    Config.label = n;
  }

  public static getVersion(): number {
    return Config.version;
  }

  public static getName(): string {
    return Config.label;
  }
}

function testStaticInit(): string {
  Config.init(2, 'myapp');
  return Config.getName() + ':' + String(Config.getVersion());
}

// --- Default value via helper ---

function withDefault(val: string | undefined, def: string): string {
  if (val !== undefined) {
    return val;
  }
  return def;
}

function testDefaultValue(): string {
  let a: string | undefined = 'hello';
  let b: string | undefined = undefined;
  let r1: string = withDefault(a, 'default');
  let r2: string = withDefault(b, 'default');
  return r1 + ',' + r2;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest27';

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
