// === Round 16: Binary search, Bubble sort, Memoization, Matrix transpose,
//   Two-pointer palindrome, Unary operators (+x, -x, !!x),
//   Chained static methods ===

// --- Binary search ---

function binarySearch(arr: number[], target: number): number {
  let lo: number = 0;
  let hi: number = arr.length - 1;
  while (lo <= hi) {
    let mid: number = lo + (hi - lo) / 2;
    mid = Math.floor(mid);
    let val: number = arr[mid];
    if (val === target) {
      return mid;
    }
    if (val < target) {
      lo = mid + 1;
    } else {
      hi = mid - 1;
    }
  }
  return -1;
}

function testBinarySearch(): string {
  let data: number[] = [1, 3, 5, 7, 9, 11, 13];
  let idx5: number = binarySearch(data, 5);
  let idx8: number = binarySearch(data, 8);
  let idx13: number = binarySearch(data, 13);
  return String(idx5) + ',' + String(idx8) + ',' + String(idx13);
}

// --- Bubble sort (nested loops with swap) ---

function bubbleSort(arr: number[]): number[] {
  let n: number = arr.length;
  for (let i: number = 0; i < n - 1; i++) {
    for (let j: number = 0; j < n - i - 1; j++) {
      if (arr[j] > arr[j + 1]) {
        let temp: number = arr[j];
        arr[j] = arr[j + 1];
        arr[j + 1] = temp;
      }
    }
  }
  return arr;
}

function testBubbleSort(): string {
  let arr: number[] = [64, 34, 25, 12, 22, 11, 90];
  let sorted: number[] = bubbleSort(arr);
  return String(sorted[0]) + ',' + String(sorted[3]) + ',' + String(sorted[6]);
}

// --- Memoization with Map cache ---

function fibonacciMemo(n: number, cache: Map<number, number>): number {
  let cached: number | undefined = cache.get(n);
  if (cached !== undefined) {
    return cached;
  }
  let result: number;
  if (n <= 1) {
    result = n;
  } else {
    let a: number = fibonacciMemo(n - 1, cache);
    let b: number = fibonacciMemo(n - 2, cache);
    result = a + b;
  }
  cache.set(n, result);
  return result;
}

function testMemoization(): string {
  let cache: Map<number, number> = new Map();
  let f10: number = fibonacciMemo(10, cache);
  return 'fib(10)=' + String(f10);
}

// --- Matrix transpose ---

function testMatrixTranspose(): string {
  let matrix: number[][] = [[1, 2, 3], [4, 5, 6]];
  let rows: number = 2;
  let cols: number = 3;
  let transposed: number[][] = [];
  for (let i: number = 0; i < cols; i++) {
    let newRow: number[] = [];
    for (let j: number = 0; j < rows; j++) {
      newRow.push(matrix[j][i]);
    }
    transposed.push(newRow);
  }
  let val: number = transposed[0][1];
  return String(transposed.length) + 'x' + String(transposed[0].length) + ':' + String(val);
}

// --- Two-pointer palindrome check ---

function isPalindrome(s: string): boolean {
  let left: number = 0;
  let right: number = s.length - 1;
  while (left < right) {
    let cLeft: string = s.charAt(left);
    let cRight: string = s.charAt(right);
    if (cLeft !== cRight) {
      return false;
    }
    left = left + 1;
    right = right - 1;
  }
  return true;
}

function testPalindrome(): string {
  let a: boolean = isPalindrome('racecar');
  let b: boolean = isPalindrome('hello');
  let c: boolean = isPalindrome('madam');
  return String(a) + ',' + String(b) + ',' + String(c);
}

// --- Unary operators ---

function testUnaryOps(x: number): string {
  let neg: number = -x;
  let not: boolean = !(x > 0);
  let dblNot: boolean = !!(x !== 0);
  return String(neg) + ',' + String(not) + ',' + String(dblNot);
}

// --- Chained static methods ---

class Calculator {
  private static result: number = 0;

  public static start(value: number): Calculator {
    Calculator.result = value;
    return new Calculator();
  }

  public add(n: number): Calculator {
    Calculator.result = Calculator.result + n;
    return this;
  }

  public multiply(n: number): Calculator {
    Calculator.result = Calculator.result * n;
    return this;
  }

  public getValue(): number {
    return Calculator.result;
  }
}

function testChainedCalc(): string {
  let result: number = Calculator.start(5).add(3).multiply(2).getValue();
  return String(result);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest16';

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
