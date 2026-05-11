// === Round 45: BST insert/search, Number parseInt with radix,
//   String case conversion, LRU cache simulation, Array.every complex,
//   deep clone via manual copy ===

// --- Binary Search Tree ---

class BSTNode {
  public value: number;
  public left: BSTNode | null;
  public right: BSTNode | null;

  constructor(value: number) {
    this.value = value;
    this.left = null;
    this.right = null;
  }
}

class BSTree {
  public root: BSTNode | null;

  constructor() {
    this.root = null;
  }

  public insert(value: number): void {
    let node: BSTNode = new BSTNode(value);
    if (this.root === null) {
      this.root = node;
      return;
    }
    let current: BSTNode | null = this.root;
    while (current !== null) {
      if (value < current.value) {
        if (current.left === null) {
          current.left = node;
          return;
        }
        current = current.left;
      } else {
        if (current.right === null) {
          current.right = node;
          return;
        }
        current = current.right;
      }
    }
  }

  public search(value: number): boolean {
    let current: BSTNode | null = this.root;
    while (current !== null) {
      if (value === current.value) {
        return true;
      }
      if (value < current.value) {
        current = current.left;
      } else {
        current = current.right;
      }
    }
    return false;
  }
}

function testBST(): string {
  let tree: BSTree = new BSTree();
  tree.insert(50);
  tree.insert(30);
  tree.insert(70);
  tree.insert(20);
  tree.insert(40);
  let found30: boolean = tree.search(30);
  let found60: boolean = tree.search(60);
  let found50: boolean = tree.search(50);
  return String(found30) + ',' + String(found60) + ',' + String(found50);
}

// --- Number parseInt with radix ---

function testParseIntRadix(): string {
  let bin: number = parseInt('1010', 2);
  let hex: number = parseInt('FF', 16);
  let oct: number = parseInt('77', 8);
  return String(bin) + ',' + String(hex) + ',' + String(oct);
}

// --- String case conversion ---

function testCaseConversion(): string {
  let lower: string = 'HELLO WORLD'.toLowerCase();
  let upper: string = 'hello world'.toUpperCase();
  let mixed: string = 'MiXeD CaSe'.toLowerCase();
  return lower + '|' + upper + '|' + String(mixed.length);
}

// --- LRU cache simulation (ordered access tracking) ---

class LRUCache {
  private capacity: number;
  private cache: Map<string, string>;
  private accessOrder: string[];

  constructor(capacity: number) {
    this.capacity = capacity;
    this.cache = new Map();
    this.accessOrder = [];
  }

  public get(key: string): string | undefined {
    let val: string | undefined = this.cache.get(key);
    if (val !== undefined) {
      this.updateOrder(key);
    }
    return val;
  }

  public put(key: string, value: string): void {
    if (this.cache.has(key)) {
      this.cache.set(key, value);
      this.updateOrder(key);
      return;
    }
    if (this.accessOrder.length >= this.capacity) {
      let oldest: string = this.accessOrder[0];
      this.cache.delete(oldest);
      this.accessOrder.splice(0, 1);
    }
    this.cache.set(key, value);
    this.accessOrder.push(key);
  }

  private updateOrder(key: string): void {
    let idx: number = this.accessOrder.indexOf(key);
    if (idx >= 0) {
      this.accessOrder.splice(idx, 1);
    }
    this.accessOrder.push(key);
  }

  public size(): number {
    return this.accessOrder.length;
  }
}

function testLRUCache(): string {
  let cache: LRUCache = new LRUCache(3);
  cache.put('a', 'value_a');
  cache.put('b', 'value_b');
  cache.put('c', 'value_c');
  let va: string | undefined = cache.get('a');
  cache.put('d', 'value_d');
  let vb: string | undefined = cache.get('b');
  let vc: string | undefined = cache.get('c');
  let sz: number = cache.size();
  return String(va) + ',' + String(vb) + ',' + String(vc) + ',' + String(sz);
}

// --- Array.every with complex predicate ---

function testEveryComplex(): string {
  let matrix: number[][] = [[2, 4, 6], [1, 3, 5], [8, 10, 12]];
  let results: boolean[] = [];
  for (let i: number = 0; i < matrix.length; i++) {
    let row: number[] = matrix[i];
    let allEven: boolean = true;
    for (let j: number = 0; j < row.length; j++) {
      if (row[j] % 2 !== 0) {
        allEven = false;
      }
    }
    results.push(allEven);
  }
  return String(results[0]) + ',' + String(results[1]) + ',' + String(results[2]);
}

// --- Deep clone via manual copy ---

function cloneRecord(source: Record<string, number>): Record<string, number> {
  let result: Record<string, number> = {};
  let keys: string[] = Object.keys(source);
  for (let i: number = 0; i < keys.length; i++) {
    let k: string = keys[i];
    result[k] = source[k];
  }
  return result;
}

function testDeepClone(): string {
  let original: Record<string, number> = {};
  original['x'] = 10;
  original['y'] = 20;
  original['z'] = 30;
  let cloned: Record<string, number> = cloneRecord(original);
  cloned['x'] = 99;
  return String(original['x']) + ',' + String(cloned['x']);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest45';

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
