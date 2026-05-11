// === Round 61: Graph DFS, backtracking (subsets),
//   dynamic programming (coin change), monotonic stack,
//   two-sum with Map ===

// --- Graph DFS ---

function dfsTraversal(graph: Map<string, string[]>, start: string): string[] {
  let visited: Set<string> = new Set();
  let result: string[] = [];
  dfsHelper(graph, start, visited, result);
  return result;
}

function dfsHelper(
  graph: Map<string, string[]>,
  node: string,
  visited: Set<string>,
  result: string[]
): void {
  visited.add(node);
  result.push(node);
  let neighbors: string[] | undefined = graph.get(node);
  if (neighbors !== undefined) {
    for (let i: number = 0; i < neighbors.length; i++) {
      let neighbor: string = neighbors[i];
      if (!visited.has(neighbor)) {
        dfsHelper(graph, neighbor, visited, result);
      }
    }
  }
}

function testDFS(): string {
  let graph: Map<string, string[]> = new Map();
  graph.set("A", ["B", "C"]);
  graph.set("B", ["A", "D", "E"]);
  graph.set("C", ["A", "F"]);
  graph.set("D", ["B"]);
  graph.set("E", ["B", "F"]);
  graph.set("F", ["C", "E"]);
  let result: string[] = dfsTraversal(graph, "A");
  return result.join(",");
}

// --- Backtracking: generate all subsets ---

function generateSubsets(elements: string[]): string[][] {
  let result: string[][] = [];
  let current: string[] = [];
  backtrackSubsets(elements, 0, current, result);
  return result;
}

function backtrackSubsets(
  elements: string[],
  index: number,
  current: string[],
  result: string[][]
): void {
  if (index === elements.length) {
    let copy: string[] = current.slice(0);
    result.push(copy);
    return;
  }
  // exclude
  backtrackSubsets(elements, index + 1, current, result);
  // include
  current.push(elements[index]);
  backtrackSubsets(elements, index + 1, current, result);
  current.pop();
}

function testSubsets(): string {
  let subsets: string[][] = generateSubsets(["a", "b", "c"]);
  return String(subsets.length);
}

// --- Dynamic programming: coin change ---

function coinChange(coins: number[], amount: number): number {
  let dp: number[] = [];
  for (let i: number = 0; i <= amount; i++) {
    dp.push(amount + 1);
  }
  dp[0] = 0;
  for (let i: number = 1; i <= amount; i++) {
    for (let j: number = 0; j < coins.length; j++) {
      if (coins[j] <= i) {
        let prev: number = dp[i - coins[j]] + 1;
        if (prev < dp[i]) {
          dp[i] = prev;
        }
      }
    }
  }
  if (dp[amount] > amount) {
    return -1;
  }
  return dp[amount];
}

function testCoinChange(): string {
  let r1: number = coinChange([1, 2, 5], 11);
  let r2: number = coinChange([2], 3);
  let r3: number = coinChange([1], 0);
  return String(r1) + "," + String(r2) + "," + String(r3);
}

// --- Monotonic stack: next greater element ---

function nextGreaterElement(nums: number[]): number[] {
  let result: number[] = [];
  let stack: number[] = [];
  for (let i: number = 0; i < nums.length; i++) {
    result.push(-1);
  }
  for (let i: number = 0; i < nums.length; i++) {
    while (stack.length > 0 && nums[i] > nums[stack[stack.length - 1]]) {
      let idx: number = stack.pop()!;
      result[idx] = nums[i];
    }
    stack.push(i);
  }
  return result;
}

function testNextGreater(): string {
  let result: number[] = nextGreaterElement([4, 5, 2, 25, 7, 8]);
  return result.join(",");
}

// --- Two-sum with Map ---

function twoSum(nums: number[], target: number): number[] {
  let seen: Map<number, number> = new Map();
  for (let i: number = 0; i < nums.length; i++) {
    let complement: number = target - nums[i];
    let found: number | undefined = seen.get(complement);
    if (found !== undefined) {
      return [found, i];
    }
    seen.set(nums[i], i);
  }
  return [];
}

function testTwoSum(): string {
  let r1: number[] = twoSum([2, 7, 11, 15], 9);
  let r2: number[] = twoSum([3, 2, 4], 6);
  return r1.join(",") + "|" + r2.join(",");
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest61';

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
