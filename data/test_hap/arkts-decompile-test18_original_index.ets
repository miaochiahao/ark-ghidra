// === Round 18: FizzBuzz, Insertion sort, Caesar cipher, Prime check,
//   Iterative fibonacci, Binary tree insert, Fast exponentiation,
//   String.codePointAt, Array.isArray checks ===

// --- FizzBuzz ---

function fizzBuzz(n: number): string {
  let result: string = '';
  for (let i: number = 1; i <= n; i++) {
    let mod3: number = i % 3;
    let mod5: number = i % 5;
    if (mod3 === 0 && mod5 === 0) {
      result = result + 'FizzBuzz';
    } else if (mod3 === 0) {
      result = result + 'Fizz';
    } else if (mod5 === 0) {
      result = result + 'Buzz';
    } else {
      result = result + String(i);
    }
    if (i < n) {
      result = result + ',';
    }
  }
  return result;
}

function testFizzBuzz(): string {
  return fizzBuzz(15);
}

// --- Insertion sort ---

function insertionSort(arr: number[]): number[] {
  let n: number = arr.length;
  for (let i: number = 1; i < n; i++) {
    let key: number = arr[i];
    let j: number = i - 1;
    while (j >= 0 && arr[j] > key) {
      arr[j + 1] = arr[j];
      j = j - 1;
    }
    arr[j + 1] = key;
  }
  return arr;
}

function testInsertionSort(): string {
  let arr: number[] = [12, 11, 13, 5, 6];
  let sorted: number[] = insertionSort(arr);
  return String(sorted[0]) + ',' + String(sorted[2]) + ',' + String(sorted[4]);
}

// --- Caesar cipher ---

function caesarEncrypt(text: string, shift: number): string {
  let result: string = '';
  for (let i: number = 0; i < text.length; i++) {
    let code: number = text.charCodeAt(i);
    if (code >= 65 && code <= 90) {
      code = 65 + (code - 65 + shift) % 26;
    } else if (code >= 97 && code <= 122) {
      code = 97 + (code - 97 + shift) % 26;
    }
    result = result + String.fromCharCode(code);
  }
  return result;
}

function testCaesarCipher(): string {
  let encrypted: string = caesarEncrypt('Hello World', 3);
  return encrypted;
}

// --- Prime check ---

function isPrime(n: number): boolean {
  if (n < 2) {
    return false;
  }
  if (n === 2) {
    return true;
  }
  if (n % 2 === 0) {
    return false;
  }
  let limit: number = Math.sqrt(n);
  let i: number = 3;
  while (i <= limit) {
    if (n % i === 0) {
      return false;
    }
    i = i + 2;
  }
  return true;
}

function testPrimeCheck(): string {
  let a: boolean = isPrime(7);
  let b: boolean = isPrime(10);
  let c: boolean = isPrime(97);
  let d: boolean = isPrime(1);
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Iterative fibonacci ---

function fibIterative(n: number): number {
  if (n <= 1) {
    return n;
  }
  let prev: number = 0;
  let curr: number = 1;
  let i: number = 2;
  while (i <= n) {
    let next: number = prev + curr;
    prev = curr;
    curr = next;
    i = i + 1;
  }
  return curr;
}

function testFibIterative(): string {
  let results: string = '';
  for (let i: number = 0; i <= 10; i++) {
    let val: number = fibIterative(i);
    if (i > 0) {
      results = results + ',';
    }
    results = results + String(val);
  }
  return results;
}

// --- Binary tree insert ---

class TreeNode {
  public value: number;
  public left: TreeNode | null;
  public right: TreeNode | null;

  constructor(value: number) {
    this.value = value;
    this.left = null;
    this.right = null;
  }
}

function treeInsert(root: TreeNode | null, value: number): TreeNode {
  if (root === null) {
    return new TreeNode(value);
  }
  if (value < root.value) {
    root.left = treeInsert(root.left, value);
  } else {
    root.right = treeInsert(root.right, value);
  }
  return root;
}

function treeHeight(node: TreeNode | null): number {
  if (node === null) {
    return 0;
  }
  let leftH: number = treeHeight(node.left);
  let rightH: number = treeHeight(node.right);
  if (leftH > rightH) {
    return leftH + 1;
  }
  return rightH + 1;
}

function testBinaryTree(): string {
  let root: TreeNode | null = null;
  root = treeInsert(root, 5);
  root = treeInsert(root, 3);
  root = treeInsert(root, 7);
  root = treeInsert(root, 1);
  root = treeInsert(root, 9);
  let h: number = treeHeight(root);
  return 'height=' + String(h);
}

// --- Fast exponentiation (repeated squaring) ---

function fastPow(base: number, exp: number): number {
  let result: number = 1;
  let b: number = base;
  let e: number = exp;
  while (e > 0) {
    if (e % 2 === 1) {
      result = result * b;
    }
    b = b * b;
    e = e / 2;
    e = Math.floor(e);
  }
  return result;
}

function testFastPow(): string {
  let a: number = fastPow(2, 10);
  let b: number = fastPow(3, 5);
  return String(a) + ',' + String(b);
}

// --- String.codePointAt and fromCodePoint ---

function testCodePoints(): string {
  let s: string = 'ABC';
  let cp0: number | undefined = s.codePointAt(0);
  let cp1: number | undefined = s.codePointAt(1);
  if (cp0 !== undefined && cp1 !== undefined) {
    let rebuilt: string = String.fromCodePoint(cp0) + String.fromCodePoint(cp1);
    return rebuilt;
  }
  return '';
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest18';

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
