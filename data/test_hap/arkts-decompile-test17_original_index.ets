// === Round 17: Array.flat (manual), Object merging, GCD algorithm,
//   Countdown loop, Nested class interaction, String building pattern,
//   Early return pattern, Boolean short-circuit as control flow ===

// --- GCD (Euclidean algorithm) ---

function gcd(a: number, b: number): number {
  let x: number = a;
  let y: number = b;
  while (y !== 0) {
    let temp: number = y;
    y = x % y;
    x = temp;
  }
  return x;
}

function testGCD(): string {
  let g1: number = gcd(48, 18);
  let g2: number = gcd(100, 75);
  let g3: number = gcd(17, 13);
  return String(g1) + ',' + String(g2) + ',' + String(g3);
}

// --- Countdown loop with early return ---

function findFirstNegative(arr: number[]): number {
  for (let i: number = 0; i < arr.length; i++) {
    if (arr[i] < 0) {
      return i;
    }
  }
  return -1;
}

function testFindFirstNegative(): string {
  let a: number[] = [3, 7, -2, 5, -8];
  let b: number[] = [1, 2, 3];
  let idx1: number = findFirstNegative(a);
  let idx2: number = findFirstNegative(b);
  return String(idx1) + ',' + String(idx2);
}

// --- Nested class interaction (one class using another) ---

class RgbColor {
  private r: number;
  private g: number;
  private b: number;

  constructor(r: number, g: number, b: number) {
    this.r = r;
    this.g = g;
    this.b = b;
  }

  public toHex(): string {
    let hex: string = '#';
    hex = hex + toHexByte(this.r);
    hex = hex + toHexByte(this.g);
    hex = hex + toHexByte(this.b);
    return hex;
  }

  public brightness(): number {
    return (this.r + this.g + this.b) / 3;
  }
}

function toHexByte(n: number): string {
  let h: string = '';
  let hi: number = n / 16;
  hi = Math.floor(hi);
  let lo: number = n % 16;
  let digits: string = '0123456789abcdef';
  h = h + digits.charAt(hi);
  h = h + digits.charAt(lo);
  return h;
}

function testColor(): string {
  let red: RgbColor = new RgbColor(255, 0, 0);
  let green: RgbColor = new RgbColor(0, 255, 0);
  let hex1: string = red.toHex();
  let hex2: string = green.toHex();
  return hex1 + ',' + hex2;
}

// --- Object merge pattern (manual copy) ---

function mergeConfigs(base: Record<string, number>, override: Record<string, number>): Record<string, number> {
  let result: Record<string, number> = {};
  let baseKeys: string[] = Object.keys(base);
  for (let i: number = 0; i < baseKeys.length; i++) {
    let key: string = baseKeys[i];
    result[key] = base[key];
  }
  let overKeys: string[] = Object.keys(override);
  for (let i: number = 0; i < overKeys.length; i++) {
    let key: string = overKeys[i];
    result[key] = override[key];
  }
  return result;
}

function testMergeConfigs(): string {
  let base: Record<string, number> = {};
  base['a'] = 1;
  base['b'] = 2;
  let over: Record<string, number> = {};
  over['b'] = 20;
  over['c'] = 30;
  let merged: Record<string, number> = mergeConfigs(base, over);
  return String(merged['a']) + ',' + String(merged['b']) + ',' + String(merged['c']);
}

// --- Countdown from N to 0 ---

function testCountdown(): string {
  let result: string = '';
  let n: number = 5;
  while (n > 0) {
    result = result + String(n);
    if (n > 1) {
      result = result + ',';
    }
    n = n - 1;
  }
  return result;
}

// --- Boolean short-circuit as guard ---

function testGuardPattern(x: number | undefined): string {
  if (x !== undefined && x > 10) {
    return 'big:' + String(x);
  }
  return 'small or missing';
}

function testGuards(): string {
  return testGuardPattern(42) + '|' + testGuardPattern(5) + '|' + testGuardPattern(undefined);
}

// --- Array every with manual loop ---

function allPositive(arr: number[]): boolean {
  for (let i: number = 0; i < arr.length; i++) {
    if (arr[i] <= 0) {
      return false;
    }
  }
  return true;
}

function testAllPositive(): string {
  let a: boolean = allPositive([1, 2, 3]);
  let b: boolean = allPositive([1, -1, 3]);
  return String(a) + ',' + String(b);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest17';

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
