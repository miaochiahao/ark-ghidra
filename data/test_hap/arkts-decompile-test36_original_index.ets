// === Round 36: Switch with multiple cases, enum-const pattern,
//   arithmetic expression chains, string parsing/validation,
//   flatten nested array (manual), regex-free pattern matching ===

// --- Switch with grouped cases ---

function seasonMonth(month: number): string {
  if (month === 3 || month === 4 || month === 5) {
    return 'spring';
  }
  if (month === 6 || month === 7 || month === 8) {
    return 'summer';
  }
  if (month === 9 || month === 10 || month === 11) {
    return 'autumn';
  }
  if (month === 12 || month === 1 || month === 2) {
    return 'winter';
  }
  return 'invalid';
}

function testSeasons(): string {
  return seasonMonth(1) + ',' + seasonMonth(4) + ',' + seasonMonth(7) + ',' + seasonMonth(10) + ',' + seasonMonth(13);
}

// --- Enum-const pattern with lookup ---

function testEnumLookup(): string {
  let names: Record<string, string> = {};
  names['0'] = 'NORTH';
  names['1'] = 'EAST';
  names['2'] = 'SOUTH';
  names['3'] = 'WEST';
  let n0: string = names['0'];
  let n2: string = names['2'];
  let keys: string[] = Object.keys(names);
  return n0 + ',' + n2 + ',' + String(keys.length);
}

// --- Arithmetic expression chains ---

function evaluate(a: number, b: number, c: number): number {
  let step1: number = a + b;
  let step2: number = step1 * c;
  let step3: number = step2 - a;
  let step4: number = step3 / (b + 1);
  return step4;
}

function testArithmeticChain(): string {
  let r1: number = evaluate(2, 3, 4);
  let r2: number = evaluate(10, 5, 2);
  return String(r1) + ',' + String(r2);
}

// --- String parsing and validation ---

function isNumeric(s: string): boolean {
  if (s.length === 0) {
    return false;
  }
  for (let i: number = 0; i < s.length; i++) {
    let code: number = s.charCodeAt(i);
    if (code < 48 || code > 57) {
      return false;
    }
  }
  return true;
}

function testStringValidation(): string {
  let r1: boolean = isNumeric('12345');
  let r2: boolean = isNumeric('12a45');
  let r3: boolean = isNumeric('');
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Flatten nested array (manual) ---

function testFlatten(): string {
  let nested: number[][] = [[1, 2], [3, 4, 5], [6]];
  let flat: number[] = [];
  for (let i: number = 0; i < nested.length; i++) {
    let inner: number[] = nested[i];
    for (let j: number = 0; j < inner.length; j++) {
      flat.push(inner[j]);
    }
  }
  return String(flat.length) + ',' + String(flat[0]) + ',' + String(flat[5]);
}

// --- Regex-free pattern matching (simple email check) ---

function simpleEmailCheck(s: string): boolean {
  let atIndex: number = s.indexOf('@');
  if (atIndex < 1) {
    return false;
  }
  let dotIndex: number = s.indexOf('.', atIndex);
  if (dotIndex < atIndex + 2) {
    return false;
  }
  if (dotIndex >= s.length - 1) {
    return false;
  }
  return true;
}

function testEmailCheck(): string {
  let r1: boolean = simpleEmailCheck('user@example.com');
  let r2: boolean = simpleEmailCheck('invalid');
  let r3: boolean = simpleEmailCheck('a@b');
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Compound boolean expressions with || ---

function testOrExpressions(): string {
  let a: number = 5;
  let b: number = 10;
  let c: number = 15;
  // Simple || comparisons (no short-circuit issue since no &&)
  let r1: boolean = a === 5;
  let r2: boolean = b === 5 || b === 10;
  let r3: boolean = c === 5 || c === 10 || c === 15;
  return String(r1) + ',' + String(r2) + ',' + String(r3);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest36';

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
