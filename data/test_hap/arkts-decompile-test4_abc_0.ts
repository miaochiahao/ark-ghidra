export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        let stringify = v13.stringify;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        throw v3;
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        throw v4;
    }
}
export class Index& {
    pkgName@entry;

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.parts = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 - v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 < 25;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        return v6.toUpperCase();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 + v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 < 0;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 < 45;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const length = v6.length;
        return length < 4;
    }

    static add(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const parts = v7.parts;
        return v10;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static swap(param_0, param_1, param_2): void     {
        const v6 = v2;
        const second = v6.second;
        const v7 = v2;
        const first = v7.first;
        return new v7.first(v4, second, first, v7);
    }

    static build(param_0, param_1, param_2): void     {
        let v6 = v2;
        const parts = v6.parts;
        v6 = " ";
        return parts.join(v6);
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        const v8 = lex_0_0;
        let message = v8.message;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static async func_main_0(param_0, param_1, param_2): void     {
        throw acc;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testJsonOps(param_0, param_1, param_2): void     {
        {  }[10] = "x";
        v6 = v4;
        v7 = "y";
        v6[20] = v7;
        v7 = JSON;
        let stringify = v7.stringify;
        stringify = v7.stringify(v4);
        return stringify;
    }

    static testMathOps(param_0, param_1, param_2): void     {
        let v13 = Math;
        let abs = v13.abs;
        let v14 = 5;
        v14 = -v14;
        abs = v13.abs(v14);
        v13 = Math;
        let max = v13.max;
        v14 = 1;
        max = v13.max(v14, 2, 3);
        v13 = Math;
        let min = v13.min;
        v14 = 1;
        v15 = 2;
        v16 = 3;
        min = v13.min(v14, v15, v16);
        v13 = Math;
        let ceil = v13.ceil;
        v14 = 3.2;
        ceil = v13.ceil(v14);
        v13 = Math;
        let floor = v13.floor;
        v14 = 3.8;
        floor = v13.floor(v14);
        v13 = Math;
        let round = v13.round;
        v14 = 3.5;
        round = v13.round(v14);
        v13 = Math;
        let sqrt = v13.sqrt;
        v14 = 16;
        sqrt = v13.sqrt(v14);
        v13 = Math;
        let pow = v13.pow;
        v14 = 2;
        v15 = 10;
        pow = v13.pow(v14, v15);
        v16 = min + max + abs;
        v15 = ceil + v16;
        v14 = floor + v15;
        v13 = round + v14;
        pow = sqrt + v13;
        return pow + pow;
    }

    static getFirst(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.first;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static getSecond(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.second;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testIncDecOps(param_0, param_1, param_2): void     {
        let v3 = 5;
        v3 = Number(v3) + 1;
        number = v3;
        number = Number(number);
        v3 = number + 1;
        number = v3;
        number = Number(number);
        v3 = number - 1;
        number = 10;
        v4 = 3 + number;
        number = v4;
        v4 = 2 - number;
        number = v4;
        v4 = 4 * number;
        number = v4;
        v4 = 2 / number;
        number = v4;
        v4 = 3 % number;
        number = v3;
        return v4 + number;
    }

    static testArrayChain(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v9: Array<unknown> = v3;
        let sort = v9.sort;
        sort = v9.sort(func_3);
        v9 = sort;
        let slice = v9.slice;
        v10 = 0;
        slice = v9.slice(v10, 5);
        v9 = slice;
        let reduce = v9.reduce;
        v10 = func_12;
        v11 = 0;
        reduce = v9.reduce(v10, v11);
        return reduce;
    }

    static testBitwiseOps(param_0, param_1, param_2, param_3, param_4): void     {
        let v13 = v3;
        const v5: number = v4 & v13;
        v13 = v22;
        const v8: number = v4 | v13;
        v13 = v22;
        const v11: number = v4 ^ v13;
        v13 = v22;
        const v7: boolean = !v13;
        v13 = v22;
        const v6: number = 2 << v13;
        v13 = v22;
        const v9: number = 2 >> v13;
        v13 = v22;
        const v10: number = 2 >>> v13;
        v13 = v9 + v6 + v7 + v11 + v8 + v5;
        return v10 + v13;
    }

    static testLogicalNot(param_0, param_1, param_2, param_3): void     { }

    static testArraySearch(param_0, param_1, param_2): void     {
        const v7: Array<unknown> = [];
        let v11: Array<unknown> = v7;
        let indexOf = v11.indexOf;
        indexOf = v11.indexOf(30);
        v11 = v7;
        let includes = v11.includes;
        v12 = 20;
        includes = v11.includes(v12);
        v11 = v7;
        let find = v11.find;
        v12 = func_11;
        find = v11.find(v12);
        let every = v11.every;
        v12 = func_16;
        every = v7.every(v12);
        let some = v11.some;
        v12 = func_21;
        some = v7.some(v12);
        return indexOf;
    }

    static testComparisons(param_0, param_1, param_2, param_3, param_4): void     {
        let v12 = v3;
        const v5: boolean = v4 === v12;
        v12 = v22;
        const v10: boolean = v4 !== v12;
        v12 = v22;
        const v8: boolean = v4 < v12;
        v12 = v22;
        const v6: boolean = v4 > v12;
        v12 = v22;
        const v9: boolean = v4 <= v12;
        v12 = v22;
        const v7: boolean = v4 >= v12;
        let v17 = String;
        v17 = v17(v5);
        v17 = String;
        v18 = v10;
        let v15: number = v17(v18) + v17;
        v17 = String;
        v17 = v8;
        let v14: number = v17(v17) + v15;
        v15 = String;
        v17 = v6;
        let v13: number = v15(v17) + v14;
        v14 = String;
        v15 = v9;
        v12 = v14(v15) + v13;
        v13 = String;
        v14 = v7;
        return v13(v14) + v12;
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testNumberFormat(param_0, param_1, param_2): void     {
        const v4 = 3.14159;
        let v8: number = v4;
        let toFixed = v8.toFixed;
        toFixed = v8.toFixed(2);
        v8 = v4;
        let toString = v8.toString;
        toString = v8.toString();
        v8 = toFixed;
        toString = `|${v8}`;
        return toString + toString;
    }

    static testTernaryChain(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 < 0)) {
        }
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        let observeComponentCreation2 = v5.observeComponentCreation2;
        let v6 = func_2;
        let v7 = Column;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        let pop = v5.pop;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testArrayTransform(param_0, param_1, param_2): void     {
        const v6: Array<unknown> = [];
        let v9: Array<unknown> = v6;
        let join = v9.join;
        join = v9.join("-");
        let map = v9.map;
        v10 = func_7;
        map = v6.map(v10);
        let some = v9.some;
        v10 = func_12;
        some = v6.some(v10);
        return join;
    }

    static testBuilderPattern(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        const v13: acc = acc;
        let add = v13.add;
        add = v13.add("Hello");
        add = add.add;
        add = "Beautiful";
        add = add.add(add);
        add = add.add;
        add = "World";
        add = add.add(add);
        let build = add.build;
        build = add.build();
        return build;
    }

    static testStringMethods2(param_0, param_1, param_2): void     {
        const v10 = "  Hello, World!  ";
        let v16: string = v10;
        let trim = v16.trim;
        trim = v16.trim();
        v16 = trim;
        let toLowerCase = v16.toLowerCase;
        toLowerCase = v16.toLowerCase();
        v16 = trim;
        let toUpperCase = v16.toUpperCase;
        toUpperCase = v16.toUpperCase();
        v16 = trim;
        let includes = v16.includes;
        includes = v16.includes("Hello");
        v16 = trim;
        let startsWith = v16.startsWith;
        v17 = "Hello";
        startsWith = v16.startsWith(v17);
        v16 = trim;
        let endsWith = v16.endsWith;
        v17 = "!";
        endsWith = v16.endsWith(v17);
        v16 = trim;
        let indexOf = v16.indexOf;
        v17 = ",";
        indexOf = v16.indexOf(v17);
        v16 = trim;
        let replace = v16.replace;
        v17 = "World";
        replace = v16.replace(v17, "ArkTS");
        v16 = "ab";
        let repeat = v16.repeat;
        v17 = 3;
        repeat = v16.repeat(v17);
        v16 = "5";
        let padStart = v16.padStart;
        v17 = 3;
        v18 = "0";
        padStart = v16.padStart(v17, v18);
        let v20 = toLowerCase;
        let v19: string = `|${v20}`;
        v20 = String;
        v18 = v20(includes) + v19;
        v17 = `|${v18}`;
        v18 = String;
        v19 = startsWith;
        v16 = v18(v19) + v17;
        padStart = `|${v16}`;
        return replace + padStart;
    }

    static testUndefinedCheck(param_0, param_1, param_2): void     {
        const v3 = undefined;
        const v5: undefined = v3;
        if (v5 === undefined) {
            return;
        }
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        let get = v7.Get;
        get = v7.Get();
        let id__ = v7.id__;
        id__ = v10.id__();
        get = v10;
        return;
    }

    static testNestedMethodCalls(param_0, param_1, param_2): void     {
        let v8 = 1;
        let v9 = 2;
        v8 = new 2();
        let swap = v8.swap;
        swap = v8.swap();
        v8 = swap;
        swap = v8.swap;
        swap = v8.swap();
        v9 = swap;
        let first = v9.getFirst;
        first = v9.getFirst();
        v9 = swap;
        return v9.getSecond() + first;
    }

    static testNumberConversions(param_0, param_1, param_2): void     {
        let v8 = parseInt;
        v8 = v8("42", 10);
        v8 = parseFloat;
        v9 = "3.14";
        v8 = v8(v9);
        v8 = isNaN;
        v9 = v8;
        v8 = v8(v9);
        v8 = isFinite;
        v9 = v8;
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `|${v14}`;
        v14 = String;
        v10 = `|${v8(v8) + v12}`;
        v11 = String;
        v9 = v11(v8) + v10;
        v8 = `|${v9}`;
        v9 = String;
        v10 = v8;
        return v9(v10) + v8;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }
}
export class @ohos.app {
    @native.ohos.app;
}
export class @ohos.curves {
    @native.ohos.curves;
}
export class @ohos.matrix4 {
    @native.ohos.matrix4;
}
export class @system.app {
    @native.system.app;
}
export class @system.curves {
    @native.system.curves;
}
export class @system.matrix4 {
    @native.system.matrix4;
}
export class @system.router {
    @native.system.router;
}
export class _ESConcurrentModuleRequestsAnnotation {
}
export class _ESExpectedPropertyCountAnnotation {
}
export class _ESSlotNumberAnnotation {
}