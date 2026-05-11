// === Round 43: Array.findIndex, enum-const pattern, merge sort,
//   String.trim, optional chaining with nullish coalescing,
//   Set intersection/difference, ArrayBuffer operations ===

// --- Array.findIndex ---

function testFindIndex(): string {
  let items: string[] = ['apple', 'banana', 'cherry', 'date'];
  let idx: number = items.findIndex((s: string): boolean => {
    return s.startsWith('c');
  });
  let notFound: number = items.findIndex((s: string): boolean => {
    return s.startsWith('z');
  });
  return String(idx) + ',' + String(notFound);
}

// --- Enum-const pattern with toString ---

class HttpStatus {
  public static OK: number = 200;
  public static NOT_FOUND: number = 404;
  public static ERROR: number = 500;

  public static toString(code: number): string {
    if (code === 200) {
      return 'OK';
    }
    if (code === 404) {
      return 'NOT_FOUND';
    }
    if (code === 500) {
      return 'ERROR';
    }
    return 'UNKNOWN';
  }
}

function testHttpStatus(): string {
  let s1: string = HttpStatus.toString(200);
  let s2: string = HttpStatus.toString(404);
  let s3: string = HttpStatus.toString(500);
  let s4: string = HttpStatus.toString(403);
  return s1 + ',' + s2 + ',' + s3 + ',' + s4;
}

// --- Merge sort (recursive divide-and-conquer) ---

function merge(left: number[], right: number[]): number[] {
  let result: number[] = [];
  let i: number = 0;
  let j: number = 0;
  while (i < left.length && j < right.length) {
    if (left[i] <= right[j]) {
      result.push(left[i]);
      i = i + 1;
    } else {
      result.push(right[j]);
      j = j + 1;
    }
  }
  while (i < left.length) {
    result.push(left[i]);
    i = i + 1;
  }
  while (j < right.length) {
    result.push(right[j]);
    j = j + 1;
  }
  return result;
}

function mergeSort(arr: number[]): number[] {
  if (arr.length <= 1) {
    return arr;
  }
  let mid: number = Math.floor(arr.length / 2);
  let left: number[] = arr.slice(0, mid);
  let right: number[] = arr.slice(mid);
  let sortedLeft: number[] = mergeSort(left);
  let sortedRight: number[] = mergeSort(right);
  return merge(sortedLeft, sortedRight);
}

function testMergeSort(): string {
  let data: number[] = [38, 27, 43, 3, 9, 82, 10];
  let sorted: number[] = mergeSort(data);
  return String(sorted[0]) + ',' + String(sorted[3]) + ',' + String(sorted[6]);
}

// --- String.trim variants ---

function testTrim(): string {
  let s1: string = '  hello  ';
  let trimmed: string = s1.trim();
  let s2: string = 'xxworldxx';
  let noTrim: string = s2.trim();
  return String(trimmed.length) + ',' + noTrim;
}

// --- Optional chaining with nullish coalescing ---

interface UserProfile {
  name: string;
  email?: string;
  age?: number;
}

function getDisplayName(profile: UserProfile | undefined): string {
  let name: string = profile?.name ?? 'Anonymous';
  let email: string = profile?.email ?? 'no-email';
  return name + '<' + email + '>';
}

function testOptionalChaining(): string {
  let p1: UserProfile = { name: 'Alice', email: 'alice@test.com', age: 30 };
  let p2: UserProfile = { name: 'Bob' };
  let r1: string = getDisplayName(p1);
  let r2: string = getDisplayName(p2);
  let r3: string = getDisplayName(undefined);
  return r1 + '|' + r2 + '|' + r3;
}

// --- Set intersection and difference ---

function testSetOperations(): string {
  let setA: Set<number> = new Set();
  setA.add(1);
  setA.add(2);
  setA.add(3);
  setA.add(4);
  let setB: Set<number> = new Set();
  setB.add(3);
  setB.add(4);
  setB.add(5);
  setB.add(6);
  let intersection: number[] = [];
  let aArr: number[] = Array.from(setA);
  for (let i: number = 0; i < aArr.length; i++) {
    if (setB.has(aArr[i])) {
      intersection.push(aArr[i]);
    }
  }
  let difference: number[] = [];
  for (let i: number = 0; i < aArr.length; i++) {
    if (!setB.has(aArr[i])) {
      difference.push(aArr[i]);
    }
  }
  return String(intersection.length) + ',' + String(difference.length) + ':' + String(intersection[0]);
}

// --- ArrayBuffer operations ---

function testArrayBuffer(): string {
  let buffer: ArrayBuffer = new ArrayBuffer(8);
  let view: DataView = new DataView(buffer);
  view.setUint8(0, 72);
  view.setUint8(1, 105);
  view.setUint8(2, 33);
  let b0: number = view.getUint8(0);
  let b1: number = view.getUint8(1);
  let b2: number = view.getUint8(2);
  return String(b0) + ',' + String(b1) + ',' + String(b2);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest43';

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
