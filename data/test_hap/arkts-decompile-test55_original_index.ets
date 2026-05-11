// === Round 55: Quick sort, graph BFS, trie,
//   producer-consumer pattern, retry with backoff ===

// --- Quick sort (in-place) ---

function quickSort(arr: number[], low: number, high: number): void {
  if (low < high) {
    let pivotIdx: number = partition(arr, low, high);
    quickSort(arr, low, pivotIdx - 1);
    quickSort(arr, pivotIdx + 1, high);
  }
}

function partition(arr: number[], low: number, high: number): number {
  let pivot: number = arr[high];
  let i: number = low - 1;
  for (let j: number = low; j < high; j++) {
    if (arr[j] < pivot) {
      i++;
      let temp: number = arr[i];
      arr[i] = arr[j];
      arr[j] = temp;
    }
  }
  let temp2: number = arr[i + 1];
  arr[i + 1] = arr[high];
  arr[high] = temp2;
  return i + 1;
}

function testQuickSort(): string {
  let arr: number[] = [10, 7, 8, 9, 1, 5];
  quickSort(arr, 0, arr.length - 1);
  return arr.join(',');
}

// --- Graph BFS ---

function bfsTraversal(graph: Map<number, number[]>, start: number): number[] {
  let visited: Set<number> = new Set();
  let queue: number[] = [];
  let result: number[] = [];
  visited.add(start);
  queue.push(start);
  while (queue.length > 0) {
    let node: number = queue.shift()!;
    result.push(node);
    let neighbors: number[] | undefined = graph.get(node);
    if (neighbors !== undefined) {
      for (let i: number = 0; i < neighbors.length; i++) {
        let neighbor: number = neighbors[i];
        if (!visited.has(neighbor)) {
          visited.add(neighbor);
          queue.push(neighbor);
        }
      }
    }
  }
  return result;
}

function testBFS(): string {
  let graph: Map<number, number[]> = new Map();
  graph.set(0, [1, 2]);
  graph.set(1, [0, 3, 4]);
  graph.set(2, [0]);
  graph.set(3, [1]);
  graph.set(4, [1]);
  let result: number[] = bfsTraversal(graph, 0);
  return result.join(',');
}

// --- Trie ---

class TrieNode {
  children: Map<string, TrieNode> = new Map();
  isEnd: boolean = false;
}

class Trie {
  root: TrieNode = new TrieNode();

  insert(word: string): void {
    let node: TrieNode = this.root;
    for (let i: number = 0; i < word.length; i++) {
      let ch: string = word.charAt(i);
      let child: TrieNode | undefined = node.children.get(ch);
      if (child === undefined) {
        child = new TrieNode();
        node.children.set(ch, child);
      }
      node = child;
    }
    node.isEnd = true;
  }

  search(word: string): boolean {
    let node: TrieNode = this.root;
    for (let i: number = 0; i < word.length; i++) {
      let ch: string = word.charAt(i);
      let child: TrieNode | undefined = node.children.get(ch);
      if (child === undefined) {
        return false;
      }
      node = child;
    }
    return node.isEnd;
  }

  startsWith(prefix: string): boolean {
    let node: TrieNode = this.root;
    for (let i: number = 0; i < prefix.length; i++) {
      let ch: string = prefix.charAt(i);
      let child: TrieNode | undefined = node.children.get(ch);
      if (child === undefined) {
        return false;
      }
      node = child;
    }
    return true;
  }
}

function testTrie(): string {
  let trie: Trie = new Trie();
  trie.insert('apple');
  trie.insert('app');
  trie.insert('application');
  let r1: boolean = trie.search('app');
  let r2: boolean = trie.search('apple');
  let r3: boolean = trie.search('appl');
  let r4: boolean = trie.startsWith('app');
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Producer-Consumer with counters ---

class ProducerConsumer {
  private buffer: number[] = [];
  private capacity: number = 5;
  private producedCount: number = 0;
  private consumedCount: number = 0;

  produce(value: number): boolean {
    if (this.buffer.length >= this.capacity) {
      return false;
    }
    this.buffer.push(value);
    this.producedCount++;
    return true;
  }

  consume(): number {
    if (this.buffer.length === 0) {
      return -1;
    }
    let value: number = this.buffer.shift()!;
    this.consumedCount++;
    return value;
  }

  getStatus(): string {
    return String(this.producedCount) + ',' + String(this.consumedCount) + ',' + String(this.buffer.length);
  }
}

function testProducerConsumer(): string {
  let pc: ProducerConsumer = new ProducerConsumer();
  pc.produce(10);
  pc.produce(20);
  pc.produce(30);
  let v1: number = pc.consume();
  pc.produce(40);
  let v2: number = pc.consume();
  let v3: number = pc.consume();
  let status: string = pc.getStatus();
  return String(v1) + ',' + String(v2) + ',' + String(v3) + '|' + status;
}

// --- Retry with exponential backoff (simulated) ---

function retryWithBackoff(attempts: number[]): string {
  let results: string[] = [];
  let delay: number = 100;
  for (let i: number = 0; i < attempts.length; i++) {
    let attempt: number = attempts[i];
    if (attempt === 0) {
      results.push('ok');
      break;
    }
    results.push('fail:' + String(delay));
    delay = delay * 2;
    if (delay > 1000) {
      delay = 1000;
    }
  }
  return results.join(',');
}

function testRetry(): string {
  return retryWithBackoff([1, 1, 0]);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest55';

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
