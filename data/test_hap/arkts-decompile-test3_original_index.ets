// === Generic Functions ===

function testGenericIdentity<T>(value: T): T {
  return value;
}

function testGenericArray<T>(items: T[]): T {
  return items[0];
}

// === Default Parameters ===

function testDefaultParam(x: number, y: number = 10): number {
  return x + y;
}

function testDefaultString(name: string, prefix: string = 'Hello'): string {
  return prefix + ' ' + name;
}

// === Optional Parameters ===

function testOptionalParam(name: string, age?: number): string {
  if (age !== undefined) {
    return name + ' is ' + String(age);
  }
  return name + ' age unknown';
}

// === Rest Parameters ===

function testRestSum(...values: number[]): number {
  let total: number = 0;
  for (const v of values) {
    total += v;
  }
  return total;
}

// === Higher-Order Functions ===

function testHigherOrder(): number {
  let multiplier = (factor: number): ((n: number) => number) => {
    return (n: number): number => n * factor;
  };
  let triple = multiplier(3);
  return triple(7);
}

// === Closure Capture ===

function testClosureCapture(): number {
  let counter: number = 0;
  let increment = (): number => {
    counter++;
    return counter;
  };
  increment();
  increment();
  return increment();
}

// === Type Aliases ===

type StringOrNumber = string | number;

function testTypeAlias(value: StringOrNumber): string {
  if (typeof value === 'string') {
    return 'string: ' + value;
  }
  return 'number: ' + String(value);
}

// === Abstract Class ===

abstract class FormBase {
  abstract calcArea(): number;

  public describe(): string {
    return 'Area: ' + String(this.calcArea());
  }
}

class RoundForm extends FormBase {
  private r: number;

  constructor(r: number) {
    super();
    this.r = r;
  }

  public calcArea(): number {
    return 3.14159 * this.r * this.r;
  }
}

class BoxForm extends FormBase {
  private w: number;
  private h: number;

  constructor(w: number, h: number) {
    super();
    this.w = w;
    this.h = h;
  }

  public calcArea(): number {
    return this.w * this.h;
  }
}

// === 'as' Type Assertion ===

function testTypeAssertion(): string {
  let value: StringOrNumber = 'hello';
  let str: string = value as string;
  return str.toUpperCase();
}

// === Interface with optional properties ===

interface UserInfo {
  name: string;
  age: number;
  email?: string;
}

function testInterfaceOptional(user: UserInfo): string {
  let result: string = user.name + ' (' + String(user.age) + ')';
  if (user.email !== undefined) {
    result = result + ' <' + user.email + '>';
  }
  return result;
}

// === Array Higher-Order Methods ===

function testArrayMethods(): number {
  let nums: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
  let evens: number[] = nums.filter((n: number): boolean => n % 2 === 0);
  let doubled: number[] = evens.map((n: number): number => n * 2);
  let sum: number = doubled.reduce((acc: number, n: number): number => acc + n, 0);
  return sum;
}

// === String methods (more complex) ===

function testStringParsing(): number {
  let input: string = '10,20,30,40,50';
  let parts: string[] = input.split(',');
  let total: number = 0;
  for (const part of parts) {
    let val: number = Number(part);
    total += val;
  }
  return total;
}

// === Nested object construction ===

function testNestedObject(): string {
  let address: Record<string, string> = {};
  address['city'] = 'Beijing';
  address['street'] = 'Chaoyang Road';

  let person: Record<string, Object> = {};
  person['name'] = 'Alice';
  person['age'] = 30;
  person['address'] = address;

  return String(person['name']);
}

// === Multiple return paths ===

function testMultipleReturns(code: number): string {
  if (code === 1) {
    return 'one';
  }
  if (code === 2) {
    return 'two';
  }
  if (code === 3) {
    return 'three';
  }
  return 'other';
}

// === Boolean logic ===

function testBooleanLogic(a: boolean, b: boolean): string {
  if (a && b) {
    return 'both true';
  }
  if (a || b) {
    return 'at least one true';
  }
  return 'both false';
}

// === Main entry ===

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest3';

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
