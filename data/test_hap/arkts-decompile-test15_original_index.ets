// === Round 15: Extended Math methods, Array.fill, Number.isFinite/isInteger,
//   Observer pattern, State machine, Nested data structures,
//   Fluent API returning this, String comparison ===

// --- Extended Math methods ---

function testExtendedMath(): string {
  let pwr: number = Math.pow(2, 10);
  let hyp: number = Math.hypot(3, 4);
  let sgn: number = Math.sign(-42);
  let trnc: number = Math.trunc(3.7);
  let lg2: number = Math.log2(256);
  let cbrt: number = Math.cbrt(27);
  return String(pwr) + ',' + String(Math.round(hyp)) + ',' +
    String(sgn) + ',' + String(trnc) + ',' + String(Math.round(lg2)) + ',' +
    String(Math.round(cbrt));
}

// --- Array.fill ---

function testArrayFill(): string {
  let arr: number[] = new Array(5);
  for (let i: number = 0; i < arr.length; i++) {
    arr[i] = 0;
  }
  for (let i: number = 0; i < arr.length; i++) {
    arr[i] = i * 10;
  }
  return String(arr[0]) + ',' + String(arr[2]) + ',' + String(arr[4]);
}

// --- Number.isFinite / Number.isInteger ---

function testNumberChecks(): string {
  let a: boolean = Number.isFinite(42);
  let b: boolean = Number.isFinite(1 / 0);
  let c: boolean = Number.isInteger(7);
  let d: boolean = Number.isInteger(3.14);
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(d);
}

// --- Observer pattern ---

class EventBus {
  private listeners: Record<string, ((data: string) => void)[]>;

  constructor() {
    this.listeners = {};
  }

  public on(event: string, callback: (data: string) => void): void {
    let list: ((data: string) => void)[] | undefined = this.listeners[event];
    if (list === undefined) {
      let newList: ((data: string) => void)[] = [];
      newList.push(callback);
      this.listeners[event] = newList;
    } else {
      list.push(callback);
    }
  }

  public emit(event: string, data: string): number {
    let list: ((data: string) => void)[] | undefined = this.listeners[event];
    if (list === undefined) {
      return 0;
    }
    return list.length;
  }
}

function testObserver(): string {
  let bus: EventBus = new EventBus();
  bus.on('click', (data: string): void => {});
  bus.on('click', (data: string): void => {});
  let count: number = bus.emit('click', 'test');
  bus.on('hover', (data: string): void => {});
  let hoverCount: number = bus.emit('hover', 'x');
  return String(count) + ',' + String(hoverCount);
}

// --- State machine ---

const STATE_IDLE: number = 0;
const STATE_RUNNING: number = 1;
const STATE_PAUSED: number = 2;
const STATE_STOPPED: number = 3;

function nextState(current: number, action: string): number {
  if (current === STATE_IDLE) {
    if (action === 'start') {
      return STATE_RUNNING;
    }
    return STATE_IDLE;
  }
  if (current === STATE_RUNNING) {
    if (action === 'pause') {
      return STATE_PAUSED;
    }
    if (action === 'stop') {
      return STATE_STOPPED;
    }
    return STATE_RUNNING;
  }
  if (current === STATE_PAUSED) {
    if (action === 'resume') {
      return STATE_RUNNING;
    }
    if (action === 'stop') {
      return STATE_STOPPED;
    }
    return STATE_PAUSED;
  }
  return STATE_STOPPED;
}

function testStateMachine(): string {
  let s1: number = nextState(STATE_IDLE, 'start');
  let s2: number = nextState(s1, 'pause');
  let s3: number = nextState(s2, 'resume');
  let s4: number = nextState(s3, 'stop');
  return String(s1) + '->' + String(s2) + '->' + String(s3) + '->' + String(s4);
}

// --- Nested data structures (3-level) ---

function testNestedData(): string {
  let data: Record<string, Record<string, number>> = {};
  let math: Record<string, number> = {};
  math['pi'] = 3;
  math['e'] = 2;
  data['math'] = math;
  let physics: Record<string, number> = {};
  physics['c'] = 299792;
  physics['g'] = 9;
  data['physics'] = physics;
  let mathSection: Record<string, number> = data['math'];
  let pi: number = mathSection['pi'];
  let physicsSection: Record<string, number> = data['physics'];
  let c: number = physicsSection['c'];
  return String(pi) + ',' + String(c);
}

// --- Fluent API (methods returning this) ---

class QueryBuilder {
  private table: string;
  private conditions: string[];
  private limitVal: number;

  constructor(table: string) {
    this.table = table;
    this.conditions = [];
    this.limitVal = 0;
  }

  public where(cond: string): QueryBuilder {
    this.conditions.push(cond);
    return this;
  }

  public limit(n: number): QueryBuilder {
    this.limitVal = n;
    return this;
  }

  public build(): string {
    let result: string = 'SELECT * FROM ' + this.table;
    if (this.conditions.length > 0) {
      result = result + ' WHERE ' + this.conditions.join(' AND ');
    }
    if (this.limitVal > 0) {
      result = result + ' LIMIT ' + String(this.limitVal);
    }
    return result;
  }
}

function testFluentAPI(): string {
  let q: QueryBuilder = new QueryBuilder('users');
  let result: string = q.where('age > 18').where('active = 1').limit(10).build();
  return result;
}

// --- String lexicographic comparison ---

function testStringCompare(): string {
  let a: string = 'apple';
  let b: string = 'banana';
  let result: string = '';
  if (a < b) {
    result = 'a<b';
  } else if (a > b) {
    result = 'a>b';
  } else {
    result = 'a=b';
  }
  return result;
}

// --- Return structured data via Record ---

function divideWithRemainder(a: number, b: number): Record<string, number> {
  let quotient: number = 0;
  let remaining: number = a;
  while (remaining >= b) {
    remaining = remaining - b;
    quotient = quotient + 1;
  }
  let result: Record<string, number> = {};
  result['quotient'] = quotient;
  result['remainder'] = remaining;
  return result;
}

function testDivideRemainder(): string {
  let r: Record<string, number> = divideWithRemainder(17, 5);
  return String(r['quotient']) + 'r' + String(r['remainder']);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest15';

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
