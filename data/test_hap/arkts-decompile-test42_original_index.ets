// === Round 42: Array.reduce multi-accumulator, String.replaceAll,
//   Object.keys iteration, Validator chain pattern, coprime check,
//   nested Map iteration, array reverse sort ===

// --- Array.reduce with object accumulator via Map ---

function testReduceGroupBy(): string {
  let words: string[] = ['ant', 'bee', 'ape', 'bat', 'bear', 'cat'];
  let groups: Map<string, string[]> = new Map();
  for (let i: number = 0; i < words.length; i++) {
    let w: string = words[i];
    let key: string = w.charAt(0);
    let existing: string[] | undefined = groups.get(key);
    if (existing === undefined) {
      let arr: string[] = [w];
      groups.set(key, arr);
    } else {
      existing.push(w);
      groups.set(key, existing);
    }
  }
  let aWords: string[] | undefined = groups.get('a');
  let bWords: string[] | undefined = groups.get('b');
  let aLen: number = 0;
  if (aWords !== undefined) {
    aLen = aWords.length;
  }
  let bLen: number = 0;
  if (bWords !== undefined) {
    bLen = bWords.length;
  }
  return 'a:' + String(aLen) + ',b:' + String(bLen);
}

// --- String.replaceAll ---

function testReplaceAll(): string {
  let text: string = 'hello world hello universe';
  let result: string = text.replaceAll('hello', 'hi');
  return result;
}

// --- Object.keys iteration ---

function testObjectKeys(): string {
  let data: Record<string, number> = {};
  data['x'] = 10;
  data['y'] = 20;
  data['z'] = 30;
  let keys: string[] = Object.keys(data);
  let total: number = 0;
  for (let i: number = 0; i < keys.length; i++) {
    let k: string = keys[i];
    total = total + data[k];
  }
  return String(keys.length) + ',' + String(total);
}

// --- Validator chain pattern ---

class Validator {
  private errors: string[];

  constructor() {
    this.errors = [];
  }

  public check(condition: boolean, message: string): Validator {
    if (!condition) {
      this.errors.push(message);
    }
    return this;
  }

  public isValid(): boolean {
    return this.errors.length === 0;
  }

  public getErrors(): string {
    return this.errors.join('; ');
  }
}

function testValidator(): string {
  let v: Validator = new Validator();
  v.check(5 > 3, '5 should be greater than 3')
    .check(10 > 20, '10 should be greater than 20')
    .check('abc'.length === 3, 'abc should have length 3')
    .check(false, 'forced error');
  let valid: boolean = v.isValid();
  let errs: string = v.getErrors();
  return String(valid) + ':' + errs;
}

// --- Coprime check using GCD ---

function gcd(a: number, b: number): number {
  while (b !== 0) {
    let temp: number = b;
    b = a % b;
    a = temp;
  }
  return a;
}

function isCoprime(a: number, b: number): boolean {
  return gcd(a, b) === 1;
}

function testCoprime(): string {
  let r1: boolean = isCoprime(14, 15);
  let r2: boolean = isCoprime(14, 21);
  let r3: boolean = isCoprime(8, 27);
  let r4: boolean = isCoprime(12, 18);
  return String(r1) + ',' + String(r2) + ',' + String(r3) + ',' + String(r4);
}

// --- Nested Map iteration ---

function testNestedMapIter(): string {
  let scores: Map<string, Map<string, number>> = new Map();
  let alice: Map<string, number> = new Map();
  alice.set('math', 90);
  alice.set('english', 85);
  let bob: Map<string, number> = new Map();
  bob.set('math', 78);
  bob.set('english', 92);
  scores.set('alice', alice);
  scores.set('bob', bob);
  let names: string[] = ['alice', 'bob'];
  let total: number = 0;
  let count: number = 0;
  for (let i: number = 0; i < names.length; i++) {
    let name: string = names[i];
    let student: Map<string, number> | undefined = scores.get(name);
    if (student !== undefined) {
      let math: number | undefined = student.get('math');
      if (math !== undefined) {
        total = total + math;
        count = count + 1;
      }
    }
  }
  return String(count) + ',' + String(total);
}

// --- Array reverse sort ---

function testReverseSort(): string {
  let nums: number[] = [3, 1, 4, 1, 5, 9, 2, 6];
  nums.sort((a: number, b: number): number => {
    return b - a;
  });
  return String(nums[0]) + ',' + String(nums[3]) + ',' + String(nums[7]);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest42';

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
