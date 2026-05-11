// === Round 53: Graph shortest path (Dijkstra simplified),
//   factorial tail recursion, mutual recursion (even/odd),
//   binary tree path sum, word ladder BFS ===

// --- Graph shortest path (simplified Dijkstra) ---

function shortestPath(edges: string[][], start: string, end: string): number {
  let dist: Map<string, number> = new Map();
  let visited: Set<string> = new Set();
  dist.set(start, 0);
  let current: string = start;
  while (current !== end && current !== '') {
    visited.add(current);
    let curDist: number | undefined = dist.get(current);
    if (curDist === undefined) {
      curDist = 999;
    }
    for (let i: number = 0; i < edges.length; i++) {
      let from: string = edges[i][0];
      let to: string = edges[i][1];
      let weight: number = 1;
      if (from === current) {
        let newDist: number = curDist + weight;
        let existingDist: number | undefined = dist.get(to);
        if (existingDist === undefined || newDist < existingDist) {
          dist.set(to, newDist);
        }
      }
    }
    let minDist: number = 999;
    let next: string = '';
    let keys: string[] = [];
    dist.forEach((v: number, k: string): void => {
      keys.push(k);
    });
    for (let i: number = 0; i < keys.length; i++) {
      let k: string = keys[i];
      if (!visited.has(k)) {
        let d: number | undefined = dist.get(k);
        if (d !== undefined && d < minDist) {
          minDist = d;
          next = k;
        }
      }
    }
    current = next;
  }
  let result: number | undefined = dist.get(end);
  if (result === undefined) {
    return -1;
  }
  return result;
}

function testShortestPath(): string {
  let edges: string[][] = [['A', 'B'], ['B', 'C'], ['A', 'C'], ['C', 'D']];
  let dist: number = shortestPath(edges, 'A', 'D');
  return String(dist);
}

// --- Factorial tail recursion ---

function factTail(n: number, acc: number): number {
  if (n <= 1) {
    return acc;
  }
  return factTail(n - 1, acc * n);
}

function factorial(n: number): number {
  return factTail(n, 1);
}

function testFactorial(): string {
  let f5: number = factorial(5);
  let f10: number = factorial(10);
  return String(f5) + ',' + String(f10);
}

// --- Mutual recursion (even/odd) ---

function isEven(n: number): boolean {
  if (n === 0) {
    return true;
  }
  return isOdd(n - 1);
}

function isOdd(n: number): boolean {
  if (n === 0) {
    return false;
  }
  return isEven(n - 1);
}

function testMutualRecursion(): string {
  let r1: boolean = isEven(4);
  let r2: boolean = isEven(7);
  let r3: boolean = isOdd(3);
  let r4: boolean = isOdd(6);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Binary tree path sum (using arrays) ---

function hasPathSum(tree: (number | null)[], index: number, target: number): boolean {
  if (index >= tree.length || tree[index] === null) {
    return false;
  }
  let val: number = tree[index] as number;
  let leftIdx: number = 2 * index + 1;
  let rightIdx: number = 2 * index + 2;
  let isLeaf: boolean = leftIdx >= tree.length || tree[leftIdx] === null;
  if (isLeaf && val === target) {
    return true;
  }
  return hasPathSum(tree, leftIdx, target - val) || hasPathSum(tree, rightIdx, target - val);
}

function testPathSum(): string {
  let tree: (number | null)[] = [5, 4, 8, 11, null, 13, 4, 7, 2, null, null, null, 1];
  let r1: boolean = hasPathSum(tree, 0, 22);
  let r2: boolean = hasPathSum(tree, 0, 26);
  let r3: boolean = hasPathSum(tree, 0, 5);
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- String compression (simple) ---

function compressString(s: string): string {
  if (s.length === 0) {
    return '';
  }
  let result: string = '';
  let count: number = 1;
  for (let i: number = 1; i < s.length; i++) {
    if (s.charAt(i) === s.charAt(i - 1)) {
      count = count + 1;
    } else {
      result = result + s.charAt(i - 1) + String(count);
      count = 1;
    }
  }
  result = result + s.charAt(s.length - 1) + String(count);
  if (result.length < s.length) {
    return result;
  }
  return s;
}

function testCompress(): string {
  let r1: string = compressString('aabcccccaaa');
  let r2: string = compressString('abc');
  return r1 + '|' + r2;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest53';

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
