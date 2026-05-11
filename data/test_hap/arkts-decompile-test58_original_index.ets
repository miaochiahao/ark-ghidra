// === Round 58: Event emitter, decorator pattern simulation,
//   strategy pattern, command pattern, iterator pattern ===

// --- Event emitter (observer pattern) ---

class EventEmitter {
  private listeners: Map<string, number[]> = new Map();

  on(event: string, callbackId: number): void {
    let existing: number[] | undefined = this.listeners.get(event);
    if (existing === undefined) {
      let arr: number[] = [];
      arr.push(callbackId);
      this.listeners.set(event, arr);
    } else {
      existing.push(callbackId);
    }
  }

  emit(event: string): number[] {
    let callbacks: number[] | undefined = this.listeners.get(event);
    if (callbacks === undefined) {
      return [];
    }
    let results: number[] = [];
    for (let i: number = 0; i < callbacks.length; i++) {
      results.push(callbacks[i]);
    }
    return results;
  }

  getListenerCount(event: string): number {
    let callbacks: number[] | undefined = this.listeners.get(event);
    if (callbacks === undefined) {
      return 0;
    }
    return callbacks.length;
  }
}

function testEventEmitter(): string {
  let emitter: EventEmitter = new EventEmitter();
  emitter.on('click', 1);
  emitter.on('click', 2);
  emitter.on('hover', 3);
  let clickCount: number = emitter.getListenerCount('click');
  let hoverCount: number = emitter.getListenerCount('hover');
  let clickResults: number[] = emitter.emit('click');
  return String(clickCount) + ',' + String(hoverCount) + ',' + clickResults.join(',');
}

// --- Strategy pattern ---

interface SortStrategy {
  sort(arr: number[]): number[];
}

class BubbleSortStrategy implements SortStrategy {
  sort(arr: number[]): number[] {
    let result: number[] = arr.slice(0);
    for (let i: number = 0; i < result.length - 1; i++) {
      for (let j: number = 0; j < result.length - 1 - i; j++) {
        if (result[j] > result[j + 1]) {
          let temp: number = result[j];
          result[j] = result[j + 1];
          result[j + 1] = temp;
        }
      }
    }
    return result;
  }
}

class SelectionSortStrategy implements SortStrategy {
  sort(arr: number[]): number[] {
    let result: number[] = arr.slice(0);
    for (let i: number = 0; i < result.length - 1; i++) {
      let minIdx: number = i;
      for (let j: number = i + 1; j < result.length; j++) {
        if (result[j] < result[minIdx]) {
          minIdx = j;
        }
      }
      if (minIdx !== i) {
        let temp: number = result[i];
        result[i] = result[minIdx];
        result[minIdx] = temp;
      }
    }
    return result;
  }
}

class Sorter {
  private strategy: SortStrategy;

  constructor(strategy: SortStrategy) {
    this.strategy = strategy;
  }

  setStrategy(strategy: SortStrategy): void {
    this.strategy = strategy;
  }

  sort(arr: number[]): number[] {
    return this.strategy.sort(arr);
  }
}

function testStrategy(): string {
  let arr: number[] = [5, 2, 8, 1, 9];
  let sorter: Sorter = new Sorter(new BubbleSortStrategy());
  let r1: number[] = sorter.sort(arr);
  sorter.setStrategy(new SelectionSortStrategy());
  let r2: number[] = sorter.sort(arr);
  return r1.join(',') + '|' + r2.join(',');
}

// --- Command pattern ---

class Light {
  state: boolean = false;

  turnOn(): void {
    this.state = true;
  }

  turnOff(): void {
    this.state = false;
  }
}

class RemoteControl {
  private light: Light;
  private history: string[] = [];

  constructor(light: Light) {
    this.light = light;
  }

  pressOn(): void {
    this.light.turnOn();
    this.history.push('on');
  }

  pressOff(): void {
    this.light.turnOff();
    this.history.push('off');
  }

  getHistory(): string {
    return this.history.join(',');
  }
}

function testCommand(): string {
  let light: Light = new Light();
  let remote: RemoteControl = new RemoteControl(light);
  remote.pressOn();
  remote.pressOff();
  remote.pressOn();
  let state: boolean = light.state;
  return String(state) + ',' + remote.getHistory();
}

// --- Iterator pattern (manual) ---

class NumberIterator {
  private data: number[] = [];
  private index: number = 0;

  constructor(data: number[]) {
    this.data = data;
    this.index = 0;
  }

  hasNext(): boolean {
    return this.index < this.data.length;
  }

  next(): number {
    let value: number = this.data[this.index];
    this.index++;
    return value;
  }

  reset(): void {
    this.index = 0;
  }
}

function testIterator(): string {
  let iter: NumberIterator = new NumberIterator([10, 20, 30]);
  let results: number[] = [];
  while (iter.hasNext()) {
    results.push(iter.next());
  }
  iter.reset();
  let first: number = iter.next();
  return results.join(',') + '|' + String(first);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest58';

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
