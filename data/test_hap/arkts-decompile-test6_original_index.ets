// === typeof Type Guard ===

function testTypeofGuard(value: string | number): string {
  if (typeof value === 'string') {
    return 'str:' + value;
  }
  return 'num:' + String(value);
}

function testTypeofBoolean(value: Object): string {
  if (typeof value === 'number') {
    return 'is number';
  }
  if (typeof value === 'string') {
    return 'is string';
  }
  if (typeof value === 'boolean') {
    return 'is boolean';
  }
  return 'is other';
}

// === Single Enum Declaration ===

const DIR_UP: number = 0;
const DIR_DOWN: number = 1;
const DIR_LEFT: number = 2;
const DIR_RIGHT: number = 3;

function testEnumValue(d: number): string {
  if (d === DIR_UP) {
    return 'going up';
  }
  if (d === DIR_DOWN) {
    return 'going down';
  }
  return 'going sideways';
}

function testEnumComparison(): boolean {
  let d: number = DIR_LEFT;
  return d === DIR_RIGHT;
}

// === RegExp Patterns ===

function testRegExp(): boolean {
  let pattern: RegExp = new RegExp('^\\d+$');
  let testStr: string = '12345';
  return pattern.test(testStr);
}

function testRegExpExec(): string {
  let pattern: RegExp = /(\w+)@(\w+)\.(\w+)/;
  let result: RegExpExecArray | null = pattern.exec('user@example.com');
  if (result !== null) {
    return result[0];
  }
  return 'no match';
}

// === ArrayBuffer / DataView ===

function testArrayBuffer(): number {
  let buffer: ArrayBuffer = new ArrayBuffer(8);
  let view: DataView = new DataView(buffer);
  view.setFloat64(0, 3.14);
  return view.getFloat64(0);
}

// === WeakRef Pattern ===

function testWeakRef(): string {
  let obj: Record<string, string> = {};
  obj['key'] = 'value';
  let ref: WeakRef<Record<string, string>> = new WeakRef(obj);
  let derefed: Record<string, string> | undefined = ref.deref();
  if (derefed !== undefined) {
    return derefed['key'];
  }
  return 'collected';
}

// === Component Lifecycle Pattern ===

class LifecycleTracker {
  private state: string;

  constructor() {
    this.state = 'created';
  }

  public aboutToAppear(): void {
    this.state = 'appeared';
  }

  public aboutToDisappear(): void {
    this.state = 'disappeared';
  }

  public onBackPress(): boolean {
    this.state = 'back_pressed';
    return true;
  }

  public getState(): string {
    return this.state;
  }
}

function testLifecycle(): string {
  let tracker: LifecycleTracker = new LifecycleTracker();
  tracker.aboutToAppear();
  let state1: string = tracker.getState();
  tracker.onBackPress();
  let state2: string = tracker.getState();
  tracker.aboutToDisappear();
  let state3: string = tracker.getState();
  return state1 + '|' + state2 + '|' + state3;
}

// === Readonly Modifier ===

class ImmutablePoint {
  readonly x: number;
  readonly y: number;

  constructor(x: number, y: number) {
    this.x = x;
    this.y = y;
  }

  public distanceTo(other: ImmutablePoint): number {
    let dx: number = this.x - other.x;
    let dy: number = this.y - other.y;
    return Math.sqrt(dx * dx + dy * dy);
  }
}

function testImmutablePoint(): number {
  let origin: ImmutablePoint = new ImmutablePoint(0, 0);
  let target: ImmutablePoint = new ImmutablePoint(3, 4);
  return target.distanceTo(origin);
}

// === Array Sort with Comparator ===

function testSortObjects(): string {
  let items: Record<string, number>[] = [];
  let item1: Record<string, number> = {};
  item1['name'] = 3;
  let item2: Record<string, number> = {};
  item2['name'] = 1;
  let item3: Record<string, number> = {};
  item3['name'] = 2;
  items.push(item1);
  items.push(item2);
  items.push(item3);
  let sorted: Record<string, number>[] = items.sort((a: Record<string, number>, b: Record<string, number>): number => {
    return a['name'] - b['name'];
  });
  return String(sorted[0]['name']) + ',' + String(sorted[1]['name']) + ',' + String(sorted[2]['name']);
}

// === Multi-dimensional array access ===

function testMatrixAccess(): number {
  let matrix: number[][] = [[1, 2, 3], [4, 5, 6], [7, 8, 9]];
  let row: number[] = matrix[1];
  let val: number = row[2];
  return val;
}

// === String padding and trimming edge cases ===

function testStringPad(): string {
  let s1: string = '42'.padStart(5, '0');
  let s2: string = 'hi'.padEnd(10, '.');
  let s3: string = '  mid  '.trimStart();
  let s4: string = '  mid  '.trimEnd();
  return s1 + '|' + s2 + '|' + s3 + '|' + s4;
}

// === Main entry ===

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest6';

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
