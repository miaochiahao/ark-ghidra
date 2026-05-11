// === Round 54: Merge sort, binary search tree, LRU cache,
//   sliding window, two-pointer technique ===

// --- Merge sort ---

function mergeSort(arr: number[]): number[] {
  if (arr.length <= 1) {
    return arr;
  }
  let mid: number = Math.floor(arr.length / 2);
  let left: number[] = mergeSort(arr.slice(0, mid));
  let right: number[] = mergeSort(arr.slice(mid));
  return merge(left, right);
}

function merge(left: number[], right: number[]): number[] {
  let result: number[] = [];
  let i: number = 0;
  let j: number = 0;
  while (i < left.length && j < right.length) {
    if (left[i] <= right[j]) {
      result.push(left[i]);
      i++;
    } else {
      result.push(right[j]);
      j++;
    }
  }
  while (i < left.length) {
    result.push(left[i]);
    i++;
  }
  while (j < right.length) {
    result.push(right[j]);
    j++;
  }
  return result;
}

function testMergeSort(): string {
  let sorted: number[] = mergeSort([5, 3, 8, 1, 9, 2, 7, 4, 6]);
  return sorted.join(',');
}

// --- Binary Search Tree ---

class TreeNode {
  value: number = 0;
  left: TreeNode | null = null;
  right: TreeNode | null = null;

  constructor(val: number) {
    this.value = val;
  }
}

function insertBST(root: TreeNode | null, val: number): TreeNode {
  if (root === null) {
    return new TreeNode(val);
  }
  if (val < root.value) {
    root.left = insertBST(root.left, val);
  } else {
    root.right = insertBST(root.right, val);
  }
  return root;
}

function searchBST(root: TreeNode | null, val: number): boolean {
  if (root === null) {
    return false;
  }
  if (root.value === val) {
    return true;
  }
  if (val < root.value) {
    return searchBST(root.left, val);
  }
  return searchBST(root.right, val);
}

function inorderTraversal(root: TreeNode | null): number[] {
  let result: number[] = [];
  if (root !== null) {
    let leftArr: number[] = inorderTraversal(root.left);
    for (let i = 0; i < leftArr.length; i++) {
      result.push(leftArr[i]);
    }
    result.push(root.value);
    let rightArr: number[] = inorderTraversal(root.right);
    for (let i = 0; i < rightArr.length; i++) {
      result.push(rightArr[i]);
    }
  }
  return result;
}

function testBST(): string {
  let root: TreeNode | null = null;
  root = insertBST(root, 5);
  root = insertBST(root, 3);
  root = insertBST(root, 7);
  root = insertBST(root, 1);
  root = insertBST(root, 4);
  let has3: boolean = searchBST(root, 3);
  let has6: boolean = searchBST(root, 6);
  let traversal: number[] = inorderTraversal(root);
  return String(has3) + ',' + String(has6) + ',' + traversal.join(',');
}

// --- LRU Cache ---

class LRUNode {
  key: string = '';
  value: number = 0;
  prev: LRUNode | null = null;
  next: LRUNode | null = null;

  constructor(k: string, v: number) {
    this.key = k;
    this.value = v;
  }
}

class LRUCache {
  private capacity: number = 0;
  private cache: Map<string, LRUNode> = new Map();
  private head: LRUNode = new LRUNode('', 0);
  private tail: LRUNode = new LRUNode('', 0);

  constructor(cap: number) {
    this.capacity = cap;
    this.head.next = this.tail;
    this.tail.prev = this.head;
  }

  get(key: string): number {
    let node: LRUNode | undefined = this.cache.get(key);
    if (node === undefined) {
      return -1;
    }
    this.removeNode(node);
    this.addToHead(node);
    return node.value;
  }

  put(key: string, value: number): void {
    let existing: LRUNode | undefined = this.cache.get(key);
    if (existing !== undefined) {
      this.removeNode(existing);
      existing.value = value;
      this.addToHead(existing);
    } else {
      let newNode: LRUNode = new LRUNode(key, value);
      this.cache.set(key, newNode);
      this.addToHead(newNode);
      if (this.cache.size > this.capacity) {
        let lru: LRUNode = this.tail.prev!;
        this.removeNode(lru);
        this.cache.delete(lru.key);
      }
    }
  }

  private removeNode(node: LRUNode): void {
    let prevNode: LRUNode | null = node.prev;
    let nextNode: LRUNode | null = node.next;
    if (prevNode !== null) {
      prevNode.next = nextNode;
    }
    if (nextNode !== null) {
      nextNode.prev = prevNode;
    }
  }

  private addToHead(node: LRUNode): void {
    node.prev = this.head;
    node.next = this.head.next;
    if (this.head.next !== null) {
      this.head.next.prev = node;
    }
    this.head.next = node;
  }
}

function testLRU(): string {
  let cache: LRUCache = new LRUCache(3);
  cache.put('a', 1);
  cache.put('b', 2);
  cache.put('c', 3);
  let v1: number = cache.get('a');
  cache.put('d', 4);
  let v2: number = cache.get('b');
  let v3: number = cache.get('c');
  let v4: number = cache.get('d');
  return v1 + ',' + v2 + ',' + v3 + ',' + v4;
}

// --- Sliding window max sum ---

function maxSubarraySum(arr: number[], k: number): number {
  if (arr.length < k) {
    return 0;
  }
  let maxSum: number = 0;
  let windowSum: number = 0;
  for (let i = 0; i < k; i++) {
    windowSum += arr[i];
  }
  maxSum = windowSum;
  for (let i = k; i < arr.length; i++) {
    windowSum = windowSum - arr[i - k] + arr[i];
    if (windowSum > maxSum) {
      maxSum = windowSum;
    }
  }
  return maxSum;
}

function testSlidingWindow(): string {
  let arr: number[] = [2, 1, 5, 1, 3, 2];
  let result: number = maxSubarraySum(arr, 3);
  return String(result);
}

// --- Two-pointer: remove duplicates in-place ---

function removeDuplicates(arr: number[]): number {
  if (arr.length === 0) {
    return 0;
  }
  let writeIdx: number = 1;
  for (let readIdx = 1; readIdx < arr.length; readIdx++) {
    if (arr[readIdx] !== arr[readIdx - 1]) {
      arr[writeIdx] = arr[readIdx];
      writeIdx++;
    }
  }
  return writeIdx;
}

function testTwoPointer(): string {
  let arr: number[] = [1, 1, 2, 2, 2, 3, 4, 4, 5];
  let len: number = removeDuplicates(arr);
  let result: number[] = [];
  for (let i = 0; i < len; i++) {
    result.push(arr[i]);
  }
  return result.join(',');
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest54';

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
