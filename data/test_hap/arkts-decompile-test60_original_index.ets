// === Round 60: Rate limiter, circular buffer, top-K,
//   run-length encoding, bracket matching ===

// --- Rate limiter (token bucket) ---

class RateLimiter {
  private tokens: number = 0;
  private maxTokens: number = 0;
  private lastRefill: number = 0;

  constructor(maxTokens: number, now: number) {
    this.tokens = maxTokens;
    this.maxTokens = maxTokens;
    this.lastRefill = now;
  }

  tryAcquire(now: number): boolean {
    let elapsed: number = now - this.lastRefill;
    let refill: number = elapsed;
    if (refill > 0) {
      let newTokens: number = this.tokens + refill;
      if (newTokens > this.maxTokens) {
        newTokens = this.maxTokens;
      }
      this.tokens = newTokens;
      this.lastRefill = now;
    }
    if (this.tokens > 0) {
      this.tokens--;
      return true;
    }
    return false;
  }
}

function testRateLimiter(): string {
  let limiter: RateLimiter = new RateLimiter(3, 0);
  let r1: boolean = limiter.tryAcquire(0);
  let r2: boolean = limiter.tryAcquire(0);
  let r3: boolean = limiter.tryAcquire(0);
  let r4: boolean = limiter.tryAcquire(0);
  let r5: boolean = limiter.tryAcquire(5);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4) + ',' + String(r5);
}

// --- Circular buffer ---

class CircularBuffer {
  private buffer: number[] = [];
  private size: number = 0;
  private head: number = 0;
  private count: number = 0;

  constructor(size: number) {
    this.size = size;
    this.buffer = [];
    this.head = 0;
    this.count = 0;
  }

  push(value: number): void {
    if (this.count < this.size) {
      this.buffer.push(value);
      this.count++;
    } else {
      this.buffer[this.head] = value;
      this.head = (this.head + 1) % this.size;
    }
  }

  toArray(): number[] {
    if (this.count < this.size) {
      return this.buffer.slice(0);
    }
    let result: number[] = [];
    for (let i: number = 0; i < this.size; i++) {
      let idx: number = (this.head + i) % this.size;
      result.push(this.buffer[idx]);
    }
    return result;
  }
}

function testCircularBuffer(): string {
  let cb: CircularBuffer = new CircularBuffer(3);
  cb.push(1);
  cb.push(2);
  cb.push(3);
  cb.push(4);
  cb.push(5);
  let result: number[] = cb.toArray();
  return result.join(',');
}

// --- Top-K with partial sort ---

function topK(arr: number[], k: number): number[] {
  let sorted: number[] = arr.slice(0);
  for (let i: number = 0; i < k; i++) {
    let maxIdx: number = i;
    for (let j: number = i + 1; j < sorted.length; j++) {
      if (sorted[j] > sorted[maxIdx]) {
        maxIdx = j;
      }
    }
    let temp: number = sorted[i];
    sorted[i] = sorted[maxIdx];
    sorted[maxIdx] = temp;
  }
  let result: number[] = [];
  for (let i: number = 0; i < k; i++) {
    result.push(sorted[i]);
  }
  return result;
}

function testTopK(): string {
  let arr: number[] = [3, 1, 4, 1, 5, 9, 2, 6, 5, 3];
  let top: number[] = topK(arr, 3);
  return top.join(',');
}

// --- Run-length encoding ---

function runLengthEncode(s: string): string {
  if (s.length === 0) {
    return '';
  }
  let result: string = '';
  let current: string = s.charAt(0);
  let count: number = 1;
  for (let i: number = 1; i < s.length; i++) {
    if (s.charAt(i) === current) {
      count++;
    } else {
      result = result + current + String(count);
      current = s.charAt(i);
      count = 1;
    }
  }
  result = result + current + String(count);
  return result;
}

function testRLE(): string {
  let r1: string = runLengthEncode('AAABBBCCDAA');
  let r2: string = runLengthEncode('XYZ');
  return r1 + '|' + r2;
}

// --- Bracket matching ---

function isBalanced(s: string): boolean {
  let stack: string[] = [];
  for (let i: number = 0; i < s.length; i++) {
    let ch: string = s.charAt(i);
    if (ch === '(' || ch === '[' || ch === '{') {
      stack.push(ch);
    } else {
      if (stack.length === 0) {
        return false;
      }
      let top: string = stack.pop()!;
      if (ch === ')' && top !== '(') {
        return false;
      }
      if (ch === ']' && top !== '[') {
        return false;
      }
      if (ch === '}' && top !== '{') {
        return false;
      }
    }
  }
  return stack.length === 0;
}

function testBracketMatch(): string {
  let r1: boolean = isBalanced('()[]{}');
  let r2: boolean = isBalanced('([)]');
  let r3: boolean = isBalanced('{[]}');
  let r4: boolean = isBalanced('(');
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest60';

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
