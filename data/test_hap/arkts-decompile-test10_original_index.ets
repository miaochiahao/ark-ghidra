// === Round 10: Multi-level inheritance, Getter/Setter accessors,
//   Complex boolean logic, Array splice/slice/reverse/includes,
//   Nested function calls, Nested Record types ===

// --- Multi-level class inheritance (3 levels) ---

class Vehicle {
  protected speed: number;

  constructor(speed: number) {
    this.speed = speed;
  }

  public getSpeed(): number {
    return this.speed;
  }

  public describe(): string {
    return 'speed=' + String(this.speed);
  }
}

class MotorVehicle extends Vehicle {
  protected fuel: number;

  constructor(speed: number, fuel: number) {
    super(speed);
    this.fuel = fuel;
  }

  public getFuel(): number {
    return this.fuel;
  }

  public describe(): string {
    return super.describe() + ',fuel=' + String(this.fuel);
  }
}

class Car extends MotorVehicle {
  private doors: number;

  constructor(speed: number, fuel: number, doors: number) {
    super(speed, fuel);
    this.doors = doors;
  }

  public getDoors(): number {
    return this.doors;
  }

  public describe(): string {
    return super.describe() + ',doors=' + String(this.doors);
  }
}

function testMultiLevelInherit(): string {
  let car: Car = new Car(100, 50, 4);
  return car.describe();
}

// --- Getter/Setter accessor properties ---

class Thermometer {
  private _celsius: number;

  constructor(celsius: number) {
    this._celsius = celsius;
  }

  get fahrenheit(): number {
    return this._celsius * 9 / 5 + 32;
  }

  set fahrenheit(f: number) {
    this._celsius = (f - 32) * 5 / 9;
  }

  public getCelsius(): number {
    return this._celsius;
  }
}

function testGetterSetter(): string {
  let t: Thermometer = new Thermometer(0);
  let f1: number = t.fahrenheit;
  t.fahrenheit = 212;
  let c: number = t.getCelsius();
  return String(f1) + '|' + String(Math.round(c));
}

// --- Complex boolean logic ---

function testBooleanLogic(a: boolean, b: boolean, c: boolean): string {
  let andResult: boolean = a && b;
  let orResult: boolean = a || b;
  let notA: boolean = !a;
  let mixed: boolean = (a && b) || c;
  let complex: boolean = !(a || b) === (!a && !b);
  let result: string = '';
  if (andResult) {
    result = result + 'A';
  }
  if (orResult) {
    result = result + 'O';
  }
  if (notA) {
    result = result + 'N';
  }
  if (mixed) {
    result = result + 'M';
  }
  if (complex) {
    result = result + 'C';
  }
  return result;
}

// --- Array splice/slice/reverse/includes ---

function testArraySpliceSlice(): string {
  let arr: number[] = [1, 2, 3, 4, 5];
  let sliced: number[] = arr.slice(1, 4);
  let has3: boolean = sliced.includes(3);
  let rev: number[] = sliced.reverse();
  return String(sliced.length) + ',' + String(has3) + ',' + String(rev[0]);
}

// --- Nested function calls (function returning function result) ---

function double(x: number): number {
  return x * 2;
}

function addOne(x: number): number {
  return x + 1;
}

function square(x: number): number {
  return x * x;
}

function testNestedCalls(): number {
  let result: number = square(addOne(double(3)));
  return result;
}

// --- Nested Record types ---

function testNestedRecord(): string {
  let person: Record<string, Record<string, number>> = {};
  let address: Record<string, number> = {};
  address['lat'] = 40;
  address['lng'] = -74;
  person['home'] = address;
  let home: Record<string, number> = person['home'];
  let lat: number = home['lat'];
  return String(lat);
}

// --- Conditional expression in variable assignment ---

function testConditionalAssign(score: number): string {
  let grade: string = '';
  if (score >= 90) {
    grade = 'A';
  } else if (score >= 80) {
    grade = 'B';
  } else if (score >= 70) {
    grade = 'C';
  } else {
    grade = 'F';
  }
  return grade;
}

function testGradeReport(): string {
  return testConditionalAssign(95) + ',' + testConditionalAssign(85) + ',' + testConditionalAssign(65);
}

// --- Method chaining on custom class ---

class StringBuilder {
  private parts: string[];

  constructor() {
    this.parts = [];
  }

  public append(s: string): StringBuilder {
    this.parts.push(s);
    return this;
  }

  public toString(): string {
    return this.parts.join('');
  }
}

function testBuilderChain(): string {
  let sb: StringBuilder = new StringBuilder();
  let result: string = sb.append('Hello').append(', ').append('World').append('!').toString();
  return result;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest10';

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
