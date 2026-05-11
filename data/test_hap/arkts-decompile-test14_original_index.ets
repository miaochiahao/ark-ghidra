// === Round 14: String.replace with function callback, Array.sort ascending/descending,
//   Queue implementation, Linked list node pattern, Date formatting,
//   Conditional chaining (nested ternary as lookup), Bitwise AND/OR masks ===

// --- String.replace with callback ---

function testReplaceCallback(): string {
  let input: string = 'hello world';
  let result: string = input.replace('world', 'arkts');
  return result;
}

// --- Array.sort ascending and descending ---

function testSortAscending(): string {
  let nums: number[] = [5, 3, 8, 1, 9, 2];
  let sorted: number[] = nums.sort((a: number, b: number): number => {
    return a - b;
  });
  return String(sorted[0]) + ',' + String(sorted[sorted.length - 1]);
}

function testSortDescending(): string {
  let nums: number[] = [5, 3, 8, 1, 9, 2];
  let sorted: number[] = nums.sort((a: number, b: number): number => {
    return b - a;
  });
  return String(sorted[0]) + ',' + String(sorted[sorted.length - 1]);
}

// --- Queue implementation (push/shift) ---

class StrQueue {
  private items: string[];

  constructor() {
    this.items = [];
  }

  public enqueue(val: string): void {
    this.items.push(val);
  }

  public dequeue(): string {
    if (this.items.length === 0) {
      return '';
    }
    let first: string = this.items[0];
    this.items.splice(0, 1);
    return first;
  }

  public front(): string {
    if (this.items.length === 0) {
      return '';
    }
    return this.items[0];
  }

  public length(): number {
    return this.items.length;
  }
}

function testQueue(): string {
  let q: StrQueue = new StrQueue();
  q.enqueue('first');
  q.enqueue('second');
  q.enqueue('third');
  let f: string = q.front();
  let d1: string = q.dequeue();
  let d2: string = q.dequeue();
  let len: number = q.length();
  return f + '|' + d1 + '|' + d2 + '|' + String(len);
}

// --- Linked list node pattern ---

class ListNode {
  public value: number;
  public next: ListNode | null;

  constructor(value: number) {
    this.value = value;
    this.next = null;
  }
}

function buildList(count: number): ListNode | null {
  let head: ListNode | null = null;
  let tail: ListNode | null = null;
  let i: number = 0;
  while (i < count) {
    let node: ListNode = new ListNode(i);
    if (head === null) {
      head = node;
      tail = node;
    } else {
      if (tail !== null) {
        tail.next = node;
      }
      tail = node;
    }
    i = i + 1;
  }
  return head;
}

function sumList(head: ListNode | null): number {
  let total: number = 0;
  let current: ListNode | null = head;
  while (current !== null) {
    total = total + current.value;
    current = current.next;
  }
  return total;
}

function testLinkedList(): string {
  let head: ListNode | null = buildList(5);
  let sum: number = sumList(head);
  return 'sum=' + String(sum);
}

// --- Bitwise AND/OR masks ---

function testBitwiseMasks(value: number): string {
  let flags: number = value;
  let hasA: boolean = (flags & 1) !== 0;
  let hasB: boolean = (flags & 2) !== 0;
  let hasC: boolean = (flags & 4) !== 0;
  let setD: number = flags | 8;
  let clearA: number = setD & (~1);
  return String(hasA) + ',' + String(hasB) + ',' + String(hasC) + ',' + String(setD) + ',' + String(clearA);
}

function testBitwiseMaskChain(): string {
  return testBitwiseMasks(7);
}

// --- Conditional chaining (manual lookup) ---

function getHttpMsg(code: number): string {
  if (code === 200) {
    return 'OK';
  }
  if (code === 301) {
    return 'Moved';
  }
  if (code === 404) {
    return 'Not Found';
  }
  if (code === 500) {
    return 'Server Error';
  }
  return 'Unknown';
}

function testHttpStatus(): string {
  return getHttpMsg(200) + ',' + getHttpMsg(404) + ',' + getHttpMsg(999);
}

// --- Date get methods ---

function testDateGet(): string {
  let d: Date = new Date(2025, 0, 15, 10, 30, 0);
  let year: number = d.getFullYear();
  let month: number = d.getMonth();
  let day: number = d.getDate();
  let hours: number = d.getHours();
  return String(year) + '-' + String(month) + '-' + String(day) + ' ' + String(hours);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest14';

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
