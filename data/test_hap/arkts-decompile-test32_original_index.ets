// === Round 32: Switch with return, nested Map iteration,
//   String.repeat/trimEnd/trimStart, Array.sort/reverse,
//   Number.isFinite/isNaN, Object.keys traversal,
//   chained string transforms, class with computed state ===

// --- Switch with return (no fall-through) ---

function httpStatusText(code: number): string {
  if (code === 200) {
    return 'OK';
  }
  if (code === 301) {
    return 'Moved';
  }
  if (code === 404) {
    return 'Not Found';
  }
  if (code === 500) {
    return 'Server Error';
  }
  return 'Unknown';
}

function testSwitchReturn(): string {
  return httpStatusText(200) + ',' + httpStatusText(404) + ',' + httpStatusText(999);
}

// --- Nested Map iteration ---

function testNestedMap(): string {
  let outer: Map<string, Map<string, number>> = new Map();
  let inner1: Map<string, number> = new Map();
  inner1.set('x', 10);
  inner1.set('y', 20);
  let inner2: Map<string, number> = new Map();
  inner2.set('x', 30);
  inner2.set('y', 40);
  outer.set('first', inner1);
  outer.set('second', inner2);

  let totalX: number = 0;
  let keys: string[] = ['first', 'second'];
  for (let i: number = 0; i < keys.length; i++) {
    let inner: Map<string, number> | undefined = outer.get(keys[i]);
    if (inner !== undefined) {
      let val: number | undefined = inner.get('x');
      if (val !== undefined) {
        totalX = totalX + val;
      }
    }
  }
  return String(totalX);
}

// --- String.repeat / trimStart / trimEnd ---

function testStringTrimRepeat(): string {
  let s: string = '  hello  ';
  let trimmed: string = s.trim();
  let left: string = s.trimStart();
  let right: string = s.trimEnd();
  let repeated: string = 'ab'.repeat(3);
  return trimmed + '|' + left + '|' + right + '|' + repeated;
}

// --- Array.sort and reverse ---

function compare(a: number, b: number): number {
  return a - b;
}

function testSortReverse(): string {
  let arr: number[] = [5, 3, 8, 1, 9, 2];
  let sorted: number[] = arr.sort(compare);
  let reversed: number[] = sorted.reverse();
  return String(reversed[0]) + ',' + String(reversed[1]) + ',' + String(reversed[5]);
}

// --- Number.isFinite / isNaN ---

function testNumberChecks(): string {
  let a: boolean = Number.isFinite(42);
  let b: boolean = Number.isFinite(Infinity);
  let c: boolean = Number.isNaN(NaN);
  let d: boolean = Number.isNaN(0);
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Object.keys traversal with accumulator ---

function testObjectAccumulate(): string {
  let scores: Record<string, number> = {};
  scores['alice'] = 85;
  scores['bob'] = 92;
  scores['carol'] = 78;
  let keys: string[] = Object.keys(scores);
  let total: number = 0;
  for (let i: number = 0; i < keys.length; i++) {
    let val: number = scores[keys[i]];
    total = total + val;
  }
  let avg: number = total / keys.length;
  return String(avg);
}

// --- Chained string transforms ---

function testChainedTransform(): string {
  let s: string = '  Hello, WORLD!  ';
  let result: string = s.trim().toLowerCase().replace('world', 'arkts');
  return result;
}

// --- Class with computed state ---

class StepCounter {
  public count: number;
  public step: number;

  constructor(step: number) {
    this.count = 0;
    this.step = step;
  }

  public increment(): void {
    this.count = this.count + this.step;
  }

  public decrement(): void {
    this.count = this.count - this.step;
  }

  public reset(): void {
    this.count = 0;
  }

  public getValue(): number {
    return this.count;
  }
}

function testStepCounter(): string {
  let c: StepCounter = new StepCounter(5);
  c.increment();
  c.increment();
  c.increment();
  c.decrement();
  let v1: number = c.getValue();
  c.reset();
  let v2: number = c.getValue();
  return String(v1) + ',' + String(v2);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest32';

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
