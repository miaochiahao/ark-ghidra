// === Round 34: Switch-case with return, boolean short-circuit,
//   nested object access, multidimensional array,
//   recursive data structure (tree depth), mutex simulation ===

// --- Switch-case with explicit returns ---

function dayType(day: number): string {
  if (day === 0) {
    return 'sunday';
  }
  if (day === 1) {
    return 'monday';
  }
  if (day === 2) {
    return 'tuesday';
  }
  if (day === 3) {
    return 'wednesday';
  }
  if (day === 4) {
    return 'thursday';
  }
  if (day === 5) {
    return 'friday';
  }
  if (day === 6) {
    return 'saturday';
  }
  return 'invalid';
}

function testDayType(): string {
  return dayType(0) + ',' + dayType(3) + ',' + dayType(6) + ',' + dayType(9);
}

// --- Boolean short-circuit evaluation ---

function testBooleanShortCircuit(): string {
  let a: boolean = true;
  let b: boolean = false;
  let c: boolean = true;
  // AND short-circuit: if first is false, skip second
  let r1: boolean = a && b;
  let r2: boolean = b && a;
  // OR short-circuit: if first is true, skip second
  let r3: boolean = a || b;
  let r4: boolean = b || c;
  // Combined
  let r5: boolean = (a || b) && c;
  let r6: boolean = !(a && b);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4) + ',' + String(r5) + ',' + String(r6);
}

// --- Nested object access (deep property chains) ---

class InnerData {
  public value: number;

  constructor(value: number) {
    this.value = value;
  }
}

class MiddleData {
  public inner: InnerData;

  constructor(inner: InnerData) {
    this.inner = inner;
  }
}

class OuterData {
  public middle: MiddleData;

  constructor(middle: MiddleData) {
    this.middle = middle;
  }

  public getDeepValue(): number {
    return this.middle.inner.value;
  }
}

function testNestedAccess(): string {
  let inner: InnerData = new InnerData(42);
  let middle: MiddleData = new MiddleData(inner);
  let outer: OuterData = new OuterData(middle);
  let val: number = outer.getDeepValue();
  return String(val);
}

// --- Multidimensional array (manual) ---

function testMultiArray(): string {
  let grid: number[][] = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
  let sum: number = 0;
  for (let i: number = 0; i < grid.length; i++) {
    let row: number[] = grid[i];
    for (let j: number = 0; j < row.length; j++) {
      sum = sum + row[j];
    }
  }
  return String(sum);
}

// --- Recursive tree depth ---

class TreeNode {
  public val: number;
  public left: TreeNode | null;
  public right: TreeNode | null;

  constructor(val: number) {
    this.val = val;
    this.left = null;
    this.right = null;
  }
}

function treeDepth(node: TreeNode | null): number {
  if (node === null) {
    return 0;
  }
  let leftDepth: number = treeDepth(node.left);
  let rightDepth: number = treeDepth(node.right);
  if (leftDepth > rightDepth) {
    return leftDepth + 1;
  }
  return rightDepth + 1;
}

function testTreeDepth(): string {
  let root: TreeNode = new TreeNode(1);
  let l: TreeNode = new TreeNode(2);
  let r: TreeNode = new TreeNode(3);
  let ll: TreeNode = new TreeNode(4);
  root.left = l;
  root.right = r;
  l.left = ll;
  let depth: number = treeDepth(root);
  return String(depth);
}

// --- Mutex simulation (lock/unlock pattern) ---

class Mutex {
  public locked: boolean;

  constructor() {
    this.locked = false;
  }

  public lock(): boolean {
    if (this.locked) {
      return false;
    }
    this.locked = true;
    return true;
  }

  public unlock(): void {
    this.locked = false;
  }

  public tryOp(label: string): string {
    if (this.lock()) {
      let result: string = label + ':ok';
      this.unlock();
      return result;
    }
    return label + ':busy';
  }
}

function testMutex(): string {
  let m: Mutex = new Mutex();
  let r1: string = m.tryOp('a');
  let r2: string = m.tryOp('b');
  m.lock();
  let r3: string = m.tryOp('c');
  m.unlock();
  return r1 + ',' + r2 + ',' + r3;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest34';

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
