// === Round 52: Sieve of Eratosthenes, simple lexer/tokenizer,
//   chained filter+map+reduce, JSON parser (manual),
//   sliding window max, longest common prefix ===

// --- Sieve of Eratosthenes ---

function sieve(limit: number): number[] {
  let isPrime: boolean[] = [];
  for (let i: number = 0; i <= limit; i++) {
    isPrime.push(true);
  }
  isPrime[0] = false;
  isPrime[1] = false;
  for (let i: number = 2; i * i <= limit; i++) {
    if (isPrime[i]) {
      for (let j: number = i * i; j <= limit; j = j + i) {
        isPrime[j] = false;
      }
    }
  }
  let primes: number[] = [];
  for (let i: number = 2; i <= limit; i++) {
    if (isPrime[i]) {
      primes.push(i);
    }
  }
  return primes;
}

function testSieve(): string {
  let primes: number[] = sieve(30);
  return String(primes.length) + ':' + String(primes[0]) + ',' + String(primes[5]) + ',' + String(primes[9]);
}

// --- Simple lexer/tokenizer ---

class Token {
  public type: string;
  public value: string;

  constructor(type: string, value: string) {
    this.type = type;
    this.value = value;
  }
}

function tokenize(input: string): Token[] {
  let tokens: Token[] = [];
  let i: number = 0;
  while (i < input.length) {
    let ch: string = input.charAt(i);
    if (ch === ' ') {
      i = i + 1;
    } else if (ch === '+') {
      tokens.push(new Token('OP', '+'));
      i = i + 1;
    } else if (ch === '-') {
      tokens.push(new Token('OP', '-'));
      i = i + 1;
    } else if (ch >= '0' && ch <= '9') {
      let num: string = '';
      while (i < input.length) {
        let c: string = input.charAt(i);
        if (c >= '0' && c <= '9') {
          num = num + c;
          i = i + 1;
        } else {
          break;
        }
      }
      tokens.push(new Token('NUM', num));
    } else {
      i = i + 1;
    }
  }
  return tokens;
}

function testTokenizer(): string {
  let tokens: Token[] = tokenize('123 + 456 - 78');
  return String(tokens.length) + ':' + tokens[0].value + ',' + tokens[1].type + ',' + tokens[4].value;
}

// --- Sliding window max ---

function slidingMax(arr: number[], k: number): number[] {
  let result: number[] = [];
  for (let i: number = 0; i <= arr.length - k; i++) {
    let max: number = arr[i];
    for (let j: number = 1; j < k; j++) {
      if (arr[i + j] > max) {
        max = arr[i + j];
      }
    }
    result.push(max);
  }
  return result;
}

function testSlidingMax(): string {
  let data: number[] = [1, 3, -1, -3, 5, 3, 6, 7];
  let result: number[] = slidingMax(data, 3);
  return String(result.length) + ':' + String(result[0]) + ',' + String(result[2]);
}

// --- Longest common prefix ---

function longestCommonPrefix(strs: string[]): string {
  if (strs.length === 0) {
    return '';
  }
  let prefix: string = strs[0];
  for (let i: number = 1; i < strs.length; i++) {
    while (strs[i].indexOf(prefix) !== 0) {
      prefix = prefix.substring(0, prefix.length - 1);
      if (prefix.length === 0) {
        return '';
      }
    }
  }
  return prefix;
}

function testLCP(): string {
  let r1: string = longestCommonPrefix(['flower', 'flow', 'flight']);
  let r2: string = longestCommonPrefix(['dog', 'racecar', 'car']);
  return r1 + '|' + r2;
}

// --- Chained filter+map+reduce on numbers ---

function testChainedOps(): string {
  let nums: number[] = [1, -2, 3, -4, 5, -6, 7, -8, 9, -10];
  let positives: number[] = nums.filter((n: number): boolean => {
    return n > 0;
  });
  let squared: number[] = positives.map((n: number): number => {
    return n * n;
  });
  let total: number = squared.reduce((sum: number, n: number): number => {
    return sum + n;
  }, 0);
  return String(positives.length) + ',' + String(total);
}

// --- Main entry ---

@Entry
@Component
struct Index {
  @State message: string = 'DecompileTest52';

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
