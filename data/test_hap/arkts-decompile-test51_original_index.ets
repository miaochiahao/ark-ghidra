// === Round 51: Generator-like state machine, Caesar cipher with key,
//   weighted random, matrix multiplication, spiral traversal,
//   RPN calculator with stack ===

// --- Caesar cipher with variable key ---

function caesarEncrypt(text: string, shift: number): string {
  let result: string = '';
  for (let i: number = 0; i < text.length; i++) {
    let ch: string = text.charAt(i);
    let code: number = ch.charCodeAt(0);
    if (code >= 65 && code <= 90) {
      code = ((code - 65 + shift) % 26 + 26) % 26 + 65;
    } else if (code >= 97 && code <= 122) {
      code = ((code - 97 + shift) % 26 + 26) % 26 + 97;
    }
    result = result + String.fromCharCode(code);
  }
  return result;
}

function caesarDecrypt(text: string, shift: number): string {
  return caesarEncrypt(text, -shift);
}

function testCaesar(): string {
  let encrypted: string = caesarEncrypt('Hello World', 3);
  let decrypted: string = caesarDecrypt(encrypted, 3);
  return encrypted + '|' + decrypted;
}

// --- Weighted random selection ---

function weightedSelect(items: string[], weights: number[], seed: number): string {
  let total: number = 0;
  for (let i: number = 0; i < weights.length; i++) {
    total = total + weights[i];
  }
  let r: number = seed % total;
  let cumulative: number = 0;
  for (let i: number = 0; i < items.length; i++) {
    cumulative = cumulative + weights[i];
    if (r < cumulative) {
      return items[i];
    }
  }
  return items[items.length - 1];
}

function testWeighted(): string {
  let items: string[] = ['A', 'B', 'C'];
  let weights: number[] = [1, 2, 3];
  let r1: string = weightedSelect(items, weights, 0);
  let r2: string = weightedSelect(items, weights, 4);
  return r1 + ',' + r2;
}

// --- Matrix multiplication ---

function matMul(a: number[][], b: number[][]): number[][] {
  let rows: number = a.length;
  let cols: number = b[0].length;
  let inner: number = b.length;
  let result: number[][] = [];
  for (let i: number = 0; i < rows; i++) {
    let row: number[] = [];
    for (let j: number = 0; j < cols; j++) {
      let sum: number = 0;
      for (let k: number = 0; k < inner; k++) {
        sum = sum + a[i][k] * b[k][j];
      }
      row.push(sum);
    }
    result.push(row);
  }
  return result;
}

function testMatMul(): string {
  let a: number[][] = [[1, 2], [3, 4]];
  let b: number[][] = [[5, 6], [7, 8]];
  let c: number[][] = matMul(a, b);
  return String(c[0][0]) + ',' + String(c[0][1]) + ',' + String(c[1][0]) + ',' + String(c[1][1]);
}

// --- RPN calculator with stack ---

class RPNCalculator {
  private stack: number[];

  constructor() {
    this.stack = [];
  }

  public push(val: number): void {
    this.stack.push(val);
  }

  public add(): number {
    let b: number = this.stack.pop() as number;
    let a: number = this.stack.pop() as number;
    let result: number = a + b;
    this.stack.push(result);
    return result;
  }

  public subtract(): number {
    let b: number = this.stack.pop() as number;
    let a: number = this.stack.pop() as number;
    let result: number = a - b;
    this.stack.push(result);
    return result;
  }

  public multiply(): number {
    let b: number = this.stack.pop() as number;
    let a: number = this.stack.pop() as number;
    let result: number = a * b;
    this.stack.push(result);
    return result;
  }

  public result(): number {
    return this.stack[this.stack.length - 1];
  }
}

function testRPN(): string {
  let calc: RPNCalculator = new RPNCalculator();
  calc.push(5);
  calc.push(3);
  calc.add();
  calc.push(2);
  calc.multiply();
  let r: number = calc.result();
  return String(r);
}

// --- Fibonacci iterative with array ---

function fibonacciIterative(n: number): number {
  if (n <= 1) {
    return n;
  }
  let fib: number[] = [0, 1];
  for (let i: number = 2; i <= n; i++) {
    fib.push(fib[i - 1] + fib[i - 2]);
  }
  return fib[n];
}

function testFibIterative(): string {
  let f10: number = fibonacciIterative(10);
  let f15: number = fibonacciIterative(15);
  let f20: number = fibonacciIterative(20);
  return String(f10) + ',' + String(f15) + ',' + String(f20);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest51';

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
