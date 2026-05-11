// === Round 37: Array.reduceRight, Set algebra (union/intersection/difference),
//   recursive deep clone, Number formatting chain, simple for-loop
//   accumulator, string building with array buffer ===

// --- Array.reduceRight (right-to-left accumulation) ---

function testReduceRight(): string {
  let arr: string[] = ['a', 'b', 'c', 'd'];
  let result: string = arr.reduceRight((acc: string, val: string): string => {
    return acc + val;
  }, '');
  return result;
}

// --- Set algebra: union, intersection, difference ---

function setUnion(a: Set<number>, b: Set<number>): Set<number> {
  let result: Set<number> = new Set(a);
  let arr: number[] = Array.from(b);
  for (let i: number = 0; i < arr.length; i++) {
    result.add(arr[i]);
  }
  return result;
}

function setIntersection(a: Set<number>, b: Set<number>): Set<number> {
  let result: Set<number> = new Set();
  let arr: number[] = Array.from(a);
  for (let i: number = 0; i < arr.length; i++) {
    if (b.has(arr[i])) {
      result.add(arr[i]);
    }
  }
  return result;
}

function setDifference(a: Set<number>, b: Set<number>): Set<number> {
  let result: Set<number> = new Set();
  let arr: number[] = Array.from(a);
  for (let i: number = 0; i < arr.length; i++) {
    if (!b.has(arr[i])) {
      result.add(arr[i]);
    }
  }
  return result;
}

function testSetAlgebra(): string {
  let a: Set<number> = new Set();
  a.add(1);
  a.add(2);
  a.add(3);
  let b: Set<number> = new Set();
  b.add(2);
  b.add(3);
  b.add(4);
  let u: Set<number> = setUnion(a, b);
  let inter: Set<number> = setIntersection(a, b);
  let diff: Set<number> = setDifference(a, b);
  return String(u.size) + ',' + String(inter.size) + ',' + String(diff.size);
}

// --- Recursive deep clone ---

class ListNode {
  public val: number;
  public next: ListNode | null;

  constructor(val: number) {
    this.val = val;
    this.next = null;
  }
}

function cloneList(head: ListNode | null): ListNode | null {
  if (head === null) {
    return null;
  }
  let node: ListNode = new ListNode(head.val);
  let rest: ListNode | null = cloneList(head.next);
  node.next = rest;
  return node;
}

function listToString(head: ListNode | null): string {
  let result: string = '';
  let current: ListNode | null = head;
  while (current !== null) {
    result = result + String(current.val) + '->';
    current = current.next;
  }
  return result + 'null';
}

function testCloneList(): string {
  let a: ListNode = new ListNode(1);
  let b: ListNode = new ListNode(2);
  let c: ListNode = new ListNode(3);
  a.next = b;
  b.next = c;
  let original: string = listToString(a);
  let cloned: ListNode | null = cloneList(a);
  let clonedStr: string = listToString(cloned);
  return original + '|' + clonedStr;
}

// --- Number formatting chain ---

function testNumberFormat(): string {
  let n: number = 3.14159265;
  let fixed2: string = n.toFixed(2);
  let fixed4: string = n.toFixed(4);
  let precision: string = n.toPrecision(4);
  let hex: string = (255).toString(16);
  let bin: string = (8).toString(2);
  return fixed2 + ',' + fixed4 + ',' + precision + ',' + hex + ',' + bin;
}

// --- Simple for-loop accumulator (no conditions inside) ---

function testSimpleLoop(): string {
  let total: number = 0;
  let arr: number[] = [1, 2, 3, 4, 5];
  for (let i: number = 0; i < arr.length; i++) {
    total = total + arr[i];
  }
  return String(total);
}

// --- String building with array buffer ---

function testStringBuilder(): string {
  let parts: string[] = [];
  parts.push('Hello');
  parts.push(' ');
  parts.push('World');
  parts.push('!');
  let result: string = parts.join('');
  return result;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest37';

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
