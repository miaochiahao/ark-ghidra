// === Round 59: Expression parser (recursive descent),
//   pipeline/chained transforms, tagged union with type narrowing,
//   string builder accumulation ===

// --- Expression parser (recursive descent) ---

function parseNumber(s: string, pos: number): number[] {
  let start: number = pos;
  while (pos < s.length && s.charAt(pos) >= '0' && s.charAt(pos) <= '9') {
    pos++;
  }
  let numStr: string = s.substring(start, pos);
  let val: number = parseInt(numStr, 10);
  return [val, pos];
}

function evaluate(s: string): number {
  let pos: number = 0;
  let result: number[] = parseNumber(s, pos);
  let left: number = result[0];
  pos = result[1];
  while (pos < s.length) {
    let op: string = s.charAt(pos);
    pos++;
    let rightResult: number[] = parseNumber(s, pos);
    let right: number = rightResult[0];
    pos = rightResult[1];
    if (op === '+') {
      left = left + right;
    } else {
      if (op === '-') {
        left = left - right;
      } else {
        if (op === '*') {
          left = left * right;
        } else {
          if (op === '/') {
            left = left / right;
          }
        }
      }
    }
  }
  return left;
}

function testEvaluate(): string {
  let r1: number = evaluate("3+5");
  let r2: number = evaluate("10-3*2");
  let r3: number = evaluate("100/4+10");
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Pipeline/chained transforms ---

class Pipeline<T> {
  private value: T;

  constructor(initial: T) {
    this.value = initial;
  }

  map(transform: (v: T) => T): Pipeline<T> {
    this.value = transform(this.value);
    return this;
  }

  filter(predicate: (v: T) => boolean, fallback: T): Pipeline<T> {
    if (!predicate(this.value)) {
      this.value = fallback;
    }
    return this;
  }

  get(): T {
    return this.value;
  }
}

function testPipeline(): string {
  let p: Pipeline<number> = new Pipeline(10);
  p.map((v: number): number => { return v * 3; });
  p.filter((v: number): boolean => { return v > 20; }, 0);
  p.map((v: number): number => { return v + 5; });
  return String(p.get());
}

// --- Tagged union with type narrowing ---

interface ShapeCircle {
  kind: string;
  radius: number;
}

interface ShapeRect {
  kind: string;
  w: number;
  h: number;
}

function getArea(shape: ShapeCircle | ShapeRect): number {
  if (shape.kind === 'circle') {
    let c: ShapeCircle = shape as ShapeCircle;
    let r: number = c.radius;
    return r * r * 3;
  }
  if (shape.kind === 'rectangle') {
    let rc: ShapeRect = shape as ShapeRect;
    return rc.w * rc.h;
  }
  return 0;
}

function getPerimeter(shape: ShapeCircle | ShapeRect): number {
  if (shape.kind === 'circle') {
    let c: ShapeCircle = shape as ShapeCircle;
    let r: number = c.radius;
    return 2 * r * 3;
  }
  if (shape.kind === 'rectangle') {
    let rc: ShapeRect = shape as ShapeRect;
    return 2 * (rc.w + rc.h);
  }
  return 0;
}

function testShapes(): string {
  let c: ShapeCircle = { kind: 'circle', radius: 5 };
  let r: ShapeRect = { kind: 'rectangle', w: 4, h: 6 };
  let a1: number = getArea(c);
  let a2: number = getArea(r);
  let p1: number = getPerimeter(c);
  let p2: number = getPerimeter(r);
  return String(a1) + ',' + String(a2) + ',' + String(p1) + ',' + String(p2);
}

// --- String builder accumulation ---

class StringBuilder {
  private parts: string[] = [];

  append(s: string): StringBuilder {
    this.parts.push(s);
    return this;
  }

  prepend(s: string): StringBuilder {
    let newParts: string[] = [s];
    for (let i: number = 0; i < this.parts.length; i++) {
      newParts.push(this.parts[i]);
    }
    this.parts = newParts;
    return this;
  }

  toString(): string {
    return this.parts.join('');
  }

  length(): number {
    let total: number = 0;
    for (let i: number = 0; i < this.parts.length; i++) {
      total = total + this.parts[i].length;
    }
    return total;
  }
}

function testStringBuilder(): string {
  let sb: StringBuilder = new StringBuilder();
  sb.append("Hello");
  sb.append(" ");
  sb.append("World");
  sb.prepend(">>> ");
  let result: string = sb.toString();
  let len: number = sb.length();
  return result + '|' + String(len);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest59';

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
