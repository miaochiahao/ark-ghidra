// === Round 9: Do-while, labeled break, array every/some/map,
//   chained method calls, number formatting, escape sequences,
//   multiple return paths, recursive function ===

// --- Do-while loop ---

function testDoWhile(): number {
  let n: number = 1;
  do {
    n = n * 2;
  } while (n < 100);
  return n;
}

// --- Array every/some/map ---

function testArrayEverySome(): string {
  let nums: number[] = [2, 4, 6, 8];
  let allEven: boolean = nums.every((n: number): boolean => {
    return n % 2 === 0;
  });
  let hasBig: boolean = nums.some((n: number): boolean => {
    return n > 5;
  });
  let doubled: number[] = nums.map((n: number): number => {
    return n * 2;
  });
  return String(allEven) + '|' + String(hasBig) + '|' + String(doubled[0]);
}

// --- Chained method calls ---

function testChainedCalls(): string {
  let result: string = '  Hello, World!  '.trim().toLowerCase().replace('world', 'arkts');
  return result;
}

// --- Multiple return paths ---

function classifyNumber(n: number): string {
  if (n > 0) {
    return 'positive';
  }
  if (n === 0) {
    return 'zero';
  }
  return 'negative';
}

function testClassify(): string {
  return classifyNumber(5) + ',' + classifyNumber(0) + ',' + classifyNumber(-3);
}

// --- Recursive function ---

function fibonacci(n: number): number {
  if (n <= 1) {
    return n;
  }
  return fibonacci(n - 1) + fibonacci(n - 2);
}

function testRecursive(): string {
  let f6: number = fibonacci(6);
  return 'fib(6)=' + String(f6);
}

// --- Escape sequences in strings ---

function testEscapeStrings(): string {
  let tab: string = 'a\tb';
  let newline: string = 'line1\nline2';
  let backslash: string = 'path\\to\\file';
  let quote: string = 'it\'s';
  return tab.length + ',' + newline.length + ',' + backslash.length + ',' + quote.length;
}

// --- Number conversions ---

function testNumberConversions(): string {
  let fromStr: number = Number('42');
  let fromInt: number = parseInt('255', 10);
  let fromHex: number = parseInt('0xFF', 16);
  let floatStr: number = parseFloat('3.14');
  let fixed: string = floatStr.toFixed(1);
  return String(fromStr) + ',' + String(fromInt) + ',' + String(fromHex) + ',' + fixed;
}

// --- Array spread (manual, no spread in ArkTS strict) ---

function testArrayConcat(): string {
  let a: number[] = [1, 2, 3];
  let b: number[] = [4, 5, 6];
  let combined: number[] = a.concat(b);
  return String(combined.length);
}

// --- Chained comparison ---

function testInRange(x: number): boolean {
  if (x > 0 && x < 100) {
    return true;
  }
  return false;
}

// --- Object merge (manual copy) ---

function testObjectMerge(): string {
  let a: Record<string, number> = {};
  a['x'] = 1;
  let b: Record<string, number> = {};
  b['y'] = 2;
  let merged: Record<string, number> = {};
  merged['x'] = a['x'];
  merged['y'] = b['y'];
  return String(merged['x']) + ',' + String(merged['y']);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest9';

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
