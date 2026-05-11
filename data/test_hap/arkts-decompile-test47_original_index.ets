// === Round 47: String.repeat dynamic, Math.min/max manual spread,
//   Promise-like deferred class, Array.from with mapping,
//   Number.toString radix, String padding patterns ===

// --- String.repeat with dynamic count ---

function repeatChar(ch: string, count: number): string {
  let result: string = '';
  for (let i: number = 0; i < count; i++) {
    result = result + ch;
  }
  return result;
}

function testRepeatChar(): string {
  let stars: string = repeatChar('*', 5);
  let dashes: string = repeatChar('-', 3);
  let dots: string = repeatChar('.', 10);
  return String(stars.length) + ',' + String(dashes.length) + ',' + String(dots.length);
}

// --- Math.min/max with manual spread ---

function minOfArray(arr: number[]): number {
  let min: number = arr[0];
  for (let i: number = 1; i < arr.length; i++) {
    if (arr[i] < min) {
      min = arr[i];
    }
  }
  return min;
}

function maxOfArray(arr: number[]): number {
  let max: number = arr[0];
  for (let i: number = 1; i < arr.length; i++) {
    if (arr[i] > max) {
      max = arr[i];
    }
  }
  return max;
}

function testMinMax(): string {
  let data: number[] = [42, 17, 93, 5, 78, 31];
  let min: number = minOfArray(data);
  let max: number = maxOfArray(data);
  return String(min) + ',' + String(max);
}

// --- Promise-like deferred class ---

class Deferred<T> {
  private resolved: boolean;
  private value: T | undefined;

  constructor() {
    this.resolved = false;
    this.value = undefined;
  }

  public resolve(val: T): void {
    this.resolved = true;
    this.value = val;
  }

  public isResolved(): boolean {
    return this.resolved;
  }

  public getValue(): T | undefined {
    return this.value;
  }
}

function testDeferred(): string {
  let d: Deferred<string> = new Deferred<string>();
  let before: boolean = d.isResolved();
  d.resolve('hello');
  let after: boolean = d.isResolved();
  let val: string | undefined = d.getValue();
  return String(before) + ',' + String(after) + ',' + String(val);
}

// --- Array.from with mapping function ---

function testArrayFrom(): string {
  let range: number[] = Array.from({ length: 5 } as ArrayLike<number>, (_, i: number): number => {
    return i * i;
  });
  let sum: number = 0;
  for (let i: number = 0; i < range.length; i++) {
    sum = sum + range[i];
  }
  return String(range.length) + ',' + String(sum);
}

// --- Number.toString radix ---

function testRadix(): string {
  let n: number = 255;
  let hex: string = n.toString(16);
  let bin: string = n.toString(2);
  let oct: string = n.toString(8);
  let dec: string = n.toString(10);
  return hex + ',' + bin.length + ',' + oct + ',' + dec;
}

// --- String padding patterns ---

function padCenter(s: string, width: number): string {
  let padLeft: number = Math.floor((width - s.length) / 2);
  let padRight: number = width - s.length - padLeft;
  let left: string = '';
  for (let i: number = 0; i < padLeft; i++) {
    left = left + ' ';
  }
  let right: string = '';
  for (let i: number = 0; i < padRight; i++) {
    right = right + ' ';
  }
  return left + s + right;
}

function testPadding(): string {
  let centered: string = padCenter('hi', 10);
  return String(centered.length) + ':' + centered;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest47';

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
