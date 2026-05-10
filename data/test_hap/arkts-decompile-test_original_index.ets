// === Control Flow Patterns ===

function testIfElse(x: number): string {
  if (x > 0) {
    return 'positive';
  } else if (x < 0) {
    return 'negative';
  } else {
    return 'zero';
  }
}

function testSwitch(value: number): string {
  switch (value) {
    case 1:
      return 'one';
    case 2:
      return 'two';
    case 3:
      return 'three';
    default:
      return 'other';
  }
}

function testForLoop(): number {
  let sum: number = 0;
  for (let i = 0; i < 10; i++) {
    sum += i;
  }
  return sum;
}

function testWhileLoop(): number {
  let count: number = 0;
  let n: number = 100;
  while (n > 1) {
    n = Math.floor(n / 2);
    count++;
  }
  return count;
}

// === Data Types ===

function testStringOps(s: string): string {
  return s.toUpperCase() + '!' + String(s.length);
}

function testArrayOps(arr: number[]): number {
  let total: number = 0;
  for (const item of arr) {
    total += item;
  }
  return total;
}

// === Class Patterns ===

class Animal {
  private name: string;
  private age: number;

  constructor(name: string, age: number) {
    this.name = name;
    this.age = age;
  }

  public speak(): string {
    return this.name + ' says hello (age ' + String(this.age) + ')';
  }

  public getAge(): number {
    return this.age;
  }
}

class Dog extends Animal {
  private breed: string;

  constructor(name: string, age: number, breed: string) {
    super(name, age);
    this.breed = breed;
  }

  public speak(): string {
    return super.speak() + ' [breed: ' + this.breed + ']';
  }
}

// === Enum Pattern (const values instead of enum merging) ===

const COLOR_RED: number = 0xFF0000;
const COLOR_GREEN: number = 0x00FF00;
const COLOR_BLUE: number = 0x0000FF;

function testColorCode(code: number): string {
  if (code === COLOR_RED) {
    return 'red';
  } else if (code === COLOR_GREEN) {
    return 'green';
  } else if (code === COLOR_BLUE) {
    return 'blue';
  } else {
    return 'unknown';
  }
}

// === Interface Pattern (no ctor-prop-decls) ===

interface Printable {
  format(): string;
}

class Book implements Printable {
  private title: string;
  private pages: number;

  constructor(title: string, pages: number) {
    this.title = title;
    this.pages = pages;
  }

  public format(): string {
    return this.title + ' (' + String(this.pages) + ' pages)';
  }
}

// === Arrow Function / Lambda ===

function testLambda(): number {
  let add = (a: number, b: number): number => a + b;
  let nums: number[] = [1, 2, 3, 4, 5];
  let doubled: number[] = nums.map((n: number): number => n * 2);
  return add(doubled[0], doubled[1]);
}

// === Recursive ===

function factorial(n: number): number {
  if (n <= 1) {
    return 1;
  }
  return n * factorial(n - 1);
}

// === Static Members ===

class MathHelper {
  static PI: number = 3.14159265;

  static circleArea(radius: number): number {
    return MathHelper.PI * radius * radius;
  }
}

// === Ternary operator ===

function testTernary(x: number): string {
  return x > 0 ? 'positive' : 'non-positive';
}

// === Do-While ===

function testDoWhile(): number {
  let count: number = 0;
  let n: number = 100;
  do {
    n = Math.floor(n / 2);
    count++;
  } while (n > 1);
  return count;
}

// === Main entry for testing ===

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest';

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
