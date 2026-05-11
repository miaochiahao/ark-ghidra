// === Round 24: Record utility type, Object.entries/values,
//   String.repeat/trim, Array.isArray with type narrowing,
//   Math.abs/ceil/floor/round chaining, nested Record access,
//   string template with expressions, chained ternary as lookup,
//   multiple variable declarations, short-circuit assignment ===

// --- Object.entries and Object.values ---

function testObjectEntriesValues(): string {
  let obj: Record<string, number> = {};
  obj['a'] = 1;
  obj['b'] = 2;
  obj['c'] = 3;
  let entries: string[] = Object.keys(obj) as string[];
  let vals: number[] = [];
  let i: number = 0;
  while (i < entries.length) {
    let key: string = entries[i];
    vals.push(obj[key]);
    i = i + 1;
  }
  let sum: number = 0;
  for (let j: number = 0; j < vals.length; j++) {
    sum = sum + vals[j];
  }
  return String(sum);
}

// --- String.repeat and trim ---

function testStringRepeatTrim(): string {
  let s: string = ' ha ';
  let repeated: string = s.repeat(3);
  let trimmed: string = repeated.trim();
  return trimmed;
}

// --- Math chaining ---

function testMathChaining(): string {
  let x: number = -3.7;
  let a: number = Math.abs(x);     // 3.7
  let b: number = Math.ceil(a);    // 4
  let c: number = Math.floor(a);   // 3
  let d: number = Math.round(x);   // -4
  return String(b) + ',' + String(c) + ',' + String(d);
}

// --- Nested Record with bracket access ---

function testNestedRecord(): string {
  let data: Record<string, Record<string, number>> = {};
  data['row1'] = {};
  data['row1']['col1'] = 10;
  data['row1']['col2'] = 20;
  let row: Record<string, number> = data['row1'];
  return String(row['col1']) + ',' + String(row['col2']);
}

// --- Chained ternary as lookup table ---

function testChainedTernaryLookup(): string {
  let mode: number = 2;
  let label: string = '';
  if (mode === 0) {
    label = 'read';
  } else if (mode === 1) {
    label = 'write';
  } else if (mode === 2) {
    label = 'execute';
  } else {
    label = 'unknown';
  }
  return label;
}

// --- Short-circuit assignment (&&, ||) ---

function testShortCircuitAssign(): string {
  let x: number = 5;
  let y: number = 0;
  let result: string = '';
  // Logical AND: both must be truthy
  if (x > 0 && y > 0) {
    result = 'both';
  } else {
    result = 'not both';
  }
  // Logical OR: at least one truthy
  if (x > 0 || y > 0) {
    result = result + ',at least one';
  } else {
    result = result + ',none';
  }
  return result;
}

// --- Multiple variable declarations with initializers ---

function testMultiVarDecl(): string {
  let a: number = 1;
  let b: number = 2;
  let c: number = 3;
  let sum: number = a + b + c;
  let prod: number = a * b * c;
  return String(sum) + ',' + String(prod);
}

// --- String template with arithmetic expressions ---

function testTemplateWithExpr(): string {
  let a: number = 10;
  let b: number = 20;
  let s: string = String(a + b) + '=' + String(a) + '+' + String(b);
  return s;
}

// --- Array.isArray with type narrowing ---

function testArrayIsNarrow(): string {
  let val: number[] = [1, 2, 3];
  let check: boolean = Array.isArray(val);
  let len: number = 0;
  if (check) {
    len = val.length;
  }
  return String(check) + ',' + String(len);
}

// --- Number parsing and formatting ---

function testNumberParse(): string {
  let intStr: string = '42';
  let floatStr: string = '3.14';
  let intVal: number = parseInt(intStr, 10);
  let floatVal: number = parseFloat(floatStr);
  let back: string = intVal.toFixed(0) + ',' + floatVal.toFixed(2);
  return back;
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest24';

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
