// === Round 48: Map entries iteration, priority queue via sort,
//   charCode range check, modulo wrapping, Object.values,
//   chained string transforms, slice with negative index ===

// --- Map entries-style iteration via keys ---

function testMapEntries(): string {
  let m: Map<string, number> = new Map();
  m.set('x', 10);
  m.set('y', 20);
  m.set('z', 30);
  let keys: string[] = [];
  m.forEach((v: number, k: string): void => {
    keys.push(k + '=' + String(v));
  });
  return String(keys.length) + ':' + keys.join(',');
}

// --- Priority queue via sorted array ---

class PriorityQueue {
  private items: number[];

  constructor() {
    this.items = [];
  }

  public enqueue(val: number): void {
    this.items.push(val);
    this.items.sort((a: number, b: number): number => {
      return a - b;
    });
  }

  public dequeue(): number {
    return this.items.shift() as number;
  }

  public peek(): number {
    return this.items[0];
  }

  public size(): number {
    return this.items.length;
  }
}

function testPriorityQueue(): string {
  let pq: PriorityQueue = new PriorityQueue();
  pq.enqueue(30);
  pq.enqueue(10);
  pq.enqueue(20);
  pq.enqueue(5);
  let first: number = pq.dequeue();
  let second: number = pq.peek();
  let sz: number = pq.size();
  return String(first) + ',' + String(second) + ',' + String(sz);
}

// --- charCode range check (isAlpha) ---

function isAlpha(ch: string): boolean {
  let code: number = ch.charCodeAt(0);
  return (code >= 65 && code <= 90) || (code >= 97 && code <= 122);
}

function testIsAlpha(): string {
  let r1: boolean = isAlpha('A');
  let r2: boolean = isAlpha('z');
  let r3: boolean = isAlpha('5');
  let r4: boolean = isAlpha('!');
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Modulo wrapping (circular index) ---

function circularAccess(arr: string[], index: number): string {
  let wrapped: number = index % arr.length;
  if (wrapped < 0) {
    wrapped = wrapped + arr.length;
  }
  return arr[wrapped];
}

function testCircularAccess(): string {
  let data: string[] = ['a', 'b', 'c', 'd', 'e'];
  let r1: string = circularAccess(data, 7);
  let r2: string = circularAccess(data, 0);
  let r3: string = circularAccess(data, 4);
  return r1 + ',' + r2 + ',' + r3;
}

// --- Object.values ---

function testObjectValues(): string {
  let scores: Record<string, number> = {};
  scores['math'] = 90;
  scores['english'] = 85;
  scores['science'] = 95;
  let vals: number[] = Object.values(scores);
  let total: number = 0;
  for (let i: number = 0; i < vals.length; i++) {
    total = total + vals[i];
  }
  return String(vals.length) + ',' + String(total);
}

// --- Chained string transforms ---

function transform(s: string): string {
  return s.trim().toLowerCase().replace(/\s+/g, '_');
}

function testTransform(): string {
  let r1: string = transform('  Hello World  ');
  let r2: string = transform('  Foo   Bar  ');
  return r1 + '|' + r2;
}

// --- Slice with negative index ---

function testSliceNegative(): string {
  let s: string = 'Hello, World!';
  let last5: string = s.slice(-5);
  let mid: string = s.slice(7, 12);
  return last5 + ',' + mid;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest48';

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
