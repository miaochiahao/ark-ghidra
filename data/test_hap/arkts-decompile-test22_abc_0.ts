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

    constructor(param_0, param_1, param_2, param_3, param_4, param_5, param_6, param_7, param_8)     {
        v2 = v17;
        v6 = v21;
        v7 = v22;
        v8 = v23;
        let v10 = v6;
        if (v10 === undefined) {
            let v10 = 1;
            v6 = -v10;
        }
        const v11 = v5;
        const v12 = v6;
        super(v10, v11, v12);
        let v10 = super(v10, v11, v12);
        v2 = v10;
        v10 = typeof v7;
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static addOne(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 + 1;
    }

    static double(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * 2;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_2 = func_0;
        v15 = func_1;
        lex_0_1 = v15;
        v15 = func_2;
        lex_0_0 = v15;
    }

    static findFirst(param_0, param_1, param_2, param_3, param_4): void     {
        const v7 = 0;
        do {
            const v9 = v3;
            const v9 = v3;
            const v10 = v7;
            const v8 = v9[v10];
        } while (v8 === v4);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testFlatten2D(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v15;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v16;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v17;
        const v4: Array<unknown> = v6;
        v6 = [];
        const v3: Array<unknown> = [];
        v7 = 0;
        const v8: number = v7;
        const v12 = v8;
        while (!(v11 < v12.length)) {
            const v12 = v3;
            let push = v12.push;
            let v13 = v8;
            const v14 = v10;
            v13 = v13[v14];
            push = v10;
            const number: number = Number(push);
            const v10: number = number + 1;
            continue;
        }
    }

    static testMathMinMax(param_0, param_1, param_2): void     {
        let v7 = Math;
        let min = v7.min;
        let v8 = 5;
        let v9 = 2;
        let v10 = 8;
        let v11 = 1;
        let v12 = 9;
        min = v7.min(v8, v9, v10, v11);
        v7 = Math;
        let max = v7.max;
        v8 = 5;
        v9 = 2;
        v10 = 8;
        v11 = 1;
        v12 = 9;
        max = v7.max(v8, v9, v10, v11);
        v8 = String;
        v9 = min;
        v8 = v8(v9);
        max = `${v8},`;
        v8 = String;
        v8 = max;
        return max + v8(v8);
    }

    static testObjectKeys(param_0, param_1, param_2): void     {
        {  }[v7] = 1;
        v6 = v4;
        v7 = "b";
        v6[v7] = 2;
        v6 = v4;
        v7 = "c";
        v6[v7] = 3;
        v7 = Object;
        let keys = v7.keys;
        keys = v7.keys(v4);
        v7 = keys;
        v8 = ",";
        return v7.join(v8);
    }

    static testStringCase(param_0, param_1, param_2): void     {
        const v4 = "Hello World";
        let v8: string = v4;
        let toUpperCase = v8.toUpperCase;
        toUpperCase = v8.toUpperCase();
        v8 = v4;
        let toLowerCase = v8.toLowerCase;
        toLowerCase = v8.toLowerCase();
        v8 = toUpperCase;
        toLowerCase = `${v8}|`;
        return toLowerCase + toLowerCase;
    }

    static testSwitchLike(param_0, param_1, param_2): void     {
        const v4 = "B";
        const v6: string = v4;
        if (v6 === "A") {
            const v3 = "Excellent";
        }
        return;
    }

    static testComposition(param_0, param_1, param_2): void     {
        const v3 = 5;
        let v5 = lex_0_1;
        v5 = v5(v3);
        v5 = lex_0_2;
        v6 = v5;
        v5 = v5(v6);
        v5 = lex_0_1;
        v5 = v5(v5);
        v5 = String;
        return v5(v5);
    }

    static testEarlyReturn(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        const v5: Array<unknown> = [];
        v7 = lex_0_0;
        v7 = v7(v5, 30);
        v7 = lex_0_0;
        v9 = 99;
        v7 = v7(v5, v9);
        v9 = String;
        v9 = v9(v7);
        v7 = `${v9},`;
        v9 = String;
        return v7 + v7(v7);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testBoolNegation(param_0, param_1, param_2): void     {
        const v3 = true;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testJsonRoundTrip(param_0, param_1, param_2): void     {
        {  }[v8] = 10;
        v7 = v4;
        v8 = "y";
        v7[v8] = 20;
        v8 = JSON;
        let stringify = v8.stringify;
        stringify = v8.stringify(v4);
        v8 = JSON;
        let parse = v8.parse;
        v9 = stringify;
        parse = v8.parse(v9);
        v9 = String;
        let v10 = parse;
        v10 = v10[v11];
        v9 = v9(v10);
        parse = `${v9},`;
        v9 = String;
        v9 = parse;
        v10 = "y";
        v9 = v9[v10];
        return parse + v9(v9);
    }

    static testNumberStatics(param_0, param_1, param_2): void     {
        let v9 = Number;
        let integer = v9.isInteger;
        integer = v9.isInteger(42);
        v9 = Number;
        integer = v9.isInteger;
        v10 = 3.14;
        integer = v9.isInteger(v10);
        v9 = Number;
        let finite = v9.isFinite;
        v10 = 100;
        finite = v9.isFinite(v10);
        v9 = Number;
        let naN = v9.isNaN;
        v10 = NaN;
        naN = v9.isNaN(v10);
        let v14 = String;
        v14 = v14(integer);
        let v12: string = `${v14},`;
        v14 = String;
        v14 = integer;
        v10 = `${v12 + v14(v14)},`;
        v11 = String;
        v12 = finite;
        v9 = v10 + v11(v12);
        naN = `${v9},`;
        v9 = String;
        v10 = naN;
        return naN + v9(v10);
    }

    static testStringCompare(param_0, param_1, param_2): void     {
        const v3 = "apple";
        const v4 = "banana";
        const v8: string = v3;
        let localeCompare = v8.localeCompare;
        localeCompare = v8.localeCompare(v4);
        localeCompare = localeCompare;
        if (!(localeCompare < 0)) {
        }
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
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