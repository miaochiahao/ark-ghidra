// === Round 21: Class inheritance chains, abstract-like pattern,
//   getter/setter pairs, static factory, method override,
//   super calls, interface-like pattern, readonly emulation ===

// --- Interface-like pattern ---

interface IGeometry {
  area(): number;
  perimeter(): number;
}

// --- Base class with constructor, getter/setter ---

class GeomBase implements IGeometry {
  protected name: string;

  constructor(name: string) {
    this.name = name;
  }

  public area(): number {
    return 0;
  }

  public perimeter(): number {
    return 0;
  }

  public get shapeName(): string {
    return this.name;
  }

  public set shapeName(val: string) {
    this.name = val;
  }

  public describe(): string {
    return this.name + ': area=' + String(this.area()) + ', perimeter=' + String(this.perimeter());
  }
}

// --- RoundShape extends GeomBase ---

class RoundShape extends GeomBase {
  private radius: number;

  constructor(radius: number) {
    super('RoundShape');
    this.radius = radius;
  }

  public area(): number {
    return Math.PI * this.radius * this.radius;
  }

  public perimeter(): number {
    return 2 * Math.PI * this.radius;
  }

  public get diameter(): number {
    return 2 * this.radius;
  }
}

// --- BoxShape extends GeomBase ---

class BoxShape extends GeomBase {
  private w: number;
  private h: number;

  constructor(w: number, h: number) {
    super('BoxShape');
    this.w = w;
    this.h = h;
  }

  public area(): number {
    return this.w * this.h;
  }

  public perimeter(): number {
    return 2 * (this.w + this.h);
  }

  public isSquare(): boolean {
    return this.w === this.h;
  }
}

// --- SquareShape extends BoxShape ---

class SquareShape extends BoxShape {
  constructor(side: number) {
    super(side, side);
  }
}

// --- Static factory ---

class GeomFactory {
  public static createRoundShape(r: number): RoundShape {
    return new RoundShape(r);
  }

  public static createBoxShape(w: number, h: number): BoxShape {
    return new BoxShape(w, h);
  }

  public static createSq(s: number): SquareShape {
    return new SquareShape(s);
  }
}

// --- Tests ---

function testShapeInheritance(): string {
  let c: RoundShape = GeomFactory.createRoundShape(5);
  let r: BoxShape = GeomFactory.createBoxShape(3, 4);
  let s: SquareShape = GeomFactory.createSq(2);
  let areaC: number = c.area();
  let areaR: number = r.area();
  let areaS: number = s.area();
  let diam: number = c.diameter;
  let isSq: boolean = s.isSquare();
  return String(areaC) + ',' + String(areaR) + ',' + String(areaS) + ',' + String(diam) + ',' + String(isSq);
}

function testGetterSetter(): string {
  let c: RoundShape = new RoundShape(3);
  let name1: string = c.shapeName;
  c.shapeName = 'MyRoundShape';
  let name2: string = c.shapeName;
  return name1 + ',' + name2;
}

function testDescribe(): string {
  let r: BoxShape = new BoxShape(3, 4);
  return r.describe();
}

function testMultiLevelInheritance(): string {
  let sq: SquareShape = new SquareShape(5);
  let perim: number = sq.perimeter();
  let isSq: boolean = sq.isSquare();
  let name: string = sq.shapeName;
  return String(perim) + ',' + String(isSq) + ',' + name;
}

// --- Polymorphism via interface ---

function totalArea(shapes: IGeometry[]): number {
  let total: number = 0;
  for (let i: number = 0; i < shapes.length; i++) {
    total = total + shapes[i].area();
  }
  return total;
}

function testPolymorphism(): string {
  let shapes: IGeometry[] = [new RoundShape(1), new BoxShape(2, 3), new SquareShape(4)];
  let total: number = totalArea(shapes);
  return String(total);
}

// --- Readonly emulation (getter-only property) ---

class ImmutablePoint {
  private _x: number;
  private _y: number;

  constructor(x: number, y: number) {
    this._x = x;
    this._y = y;
  }

  public get x(): number {
    return this._x;
  }

  public get y(): number {
    return this._y;
  }

  public distanceTo(other: ImmutablePoint): number {
    let dx: number = this._x - other.x;
    let dy: number = this._y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}

function testImmutablePoint(): string {
  let p1: ImmutablePoint = new ImmutablePoint(0, 0);
  let p2: ImmutablePoint = new ImmutablePoint(3, 4);
  let dist: number = p1.distanceTo(p2);
  return String(dist);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest21';

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
