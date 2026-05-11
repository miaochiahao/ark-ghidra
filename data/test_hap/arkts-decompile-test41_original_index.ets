// === Round 41: RegExp exec/test, Array.sort complex comparator,
//   StringBuilder chain, switch with counters, String.split with limit,
//   Array.entries iteration, nested ternary ===

// --- RegExp exec and test ---

function testRegExpExec(): string {
  let pattern: RegExp = /(\d+)-(\d+)/;
  let text: string = 'abc 123-456 def 789-012';
  let match: RegExpExecArray | null = pattern.exec(text);
  if (match !== null) {
    let full: string = match[0];
    let g1: string = match[1];
    let g2: string = match[2];
    return full + ',' + g1 + ',' + g2;
  }
  return 'no match';
}

function testRegExpTest(): string {
  let email: string = 'user@example.com';
  let bad: string = 'not-email';
  let re: RegExp = /^[a-zA-Z]+@[a-zA-Z]+\.[a-zA-Z]+$/;
  let valid: boolean = re.test(email);
  let invalid: boolean = re.test(bad);
  return String(valid) + ',' + String(invalid);
}

// --- Array.sort with complex comparator ---

function testSortComparator(): string {
  let items: string[] = ['banana', 'apple', 'cherry', 'date'];
  items.sort((a: string, b: string): number => {
    let la: string = a.charAt(0);
    let lb: string = b.charAt(0);
    if (la !== lb) {
      return la < lb ? -1 : 1;
    }
    return a.length - b.length;
  });
  return items[0] + ',' + items[1] + ',' + items[2] + ',' + items[3];
}

// --- StringBuilder chain pattern ---

class StringBuilder {
  private parts: string[];

  constructor() {
    this.parts = [];
  }

  public append(s: string): StringBuilder {
    this.parts.push(s);
    return this;
  }

  public prepend(s: string): StringBuilder {
    this.parts.splice(0, 0, s);
    return this;
  }

  public build(): string {
    return this.parts.join('');
  }

  public length(): number {
    let total: number = 0;
    for (let i: number = 0; i < this.parts.length; i++) {
      total = total + this.parts[i].length;
    }
    return total;
  }
}

function testStringBuilder(): string {
  let sb: StringBuilder = new StringBuilder();
  let result: string = sb.append('Hello').append(', ').append('World').append('!').build();
  let len: number = new StringBuilder().append('test').length();
  return result + '|' + String(len);
}

// --- Switch with counters ---

function countGrades(scores: number[]): string {
  let a: number = 0;
  let b: number = 0;
  let c: number = 0;
  let f: number = 0;
  for (let i: number = 0; i < scores.length; i++) {
    let s: number = scores[i];
    if (s >= 90) {
      a = a + 1;
    } else if (s >= 80) {
      b = b + 1;
    } else if (s >= 70) {
      c = c + 1;
    } else {
      f = f + 1;
    }
  }
  return String(a) + ',' + String(b) + ',' + String(c) + ',' + String(f);
}

function testCountGrades(): string {
  let scores: number[] = [95, 82, 71, 65, 99, 88, 73, 55];
  return countGrades(scores);
}

// --- String.split with limit ---

function testSplitLimit(): string {
  let csv: string = 'a,b,c,d,e,f';
  let parts3: string[] = csv.split(',', 3);
  let parts5: string[] = csv.split(',', 5);
  return String(parts3.length) + ':' + parts3[2] + '|' + String(parts5.length) + ':' + parts5[4];
}

// --- Array.entries iteration ---

function testEntriesIteration(): string {
  let fruits: string[] = ['apple', 'banana', 'cherry'];
  let result: string = '';
  for (let i: number = 0; i < fruits.length; i++) {
    let entry: string = String(i) + '=' + fruits[i];
    if (result.length > 0) {
      result = result + ';';
    }
    result = result + entry;
  }
  return result;
}

// --- Nested ternary ---

function classifyScore(score: number): string {
  return score >= 90 ? 'A' : score >= 80 ? 'B' : score >= 70 ? 'C' : score >= 60 ? 'D' : 'F';
}

function testClassifyScore(): string {
  return classifyScore(95) + ',' + classifyScore(85) + ',' + classifyScore(75) + ',' + classifyScore(55);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest41';

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
