export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw v6;
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
        throw v6;
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
        const v10 = v6;
        if (v10 === undefined) {
            const v10 = 1;
            v6 = -v10;
        } else {
            let isUndefined: boolean = v10 === undefined;
        }
        let isUndefined: boolean = isUndefined === undefined;
        const v11 = v5;
        const v12 = v6;
        super(isUndefined, v11, v12);
        isUndefined = super(isUndefined, v11, v12);
        v2 = isUndefined;
        isUndefined = typeof v7;
        throw isUndefined === "function";
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
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

    static func_main_0(param_0, param_1, param_2): void     { }

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

    static testUint8Array(param_0, param_1, param_2): void     {
        let v8 = Uint8Array;
        let v9 = 3;
        v8 = new Uint8Array();
        v9 = 0;
        v8[v9] = 65;
        v8 = uint8Array;
        v9 = 1;
        v8[v9] = 66;
        v8 = uint8Array;
        v9 = 2;
        v8[v9] = 67;
        v8 = uint8Array;
        v9 = 0;
        const v3 = v8[v9];
        v8 = uint8Array;
        v9 = 1;
        const v4 = v8[v9];
        v8 = uint8Array;
        v9 = 2;
        const v5 = v8[v9];
        let v11 = String;
        let fromCharCode = v11.fromCharCode;
        fromCharCode = v11.fromCharCode(v3);
        v11 = String;
        fromCharCode = v11.fromCharCode;
        v8 = fromCharCode2 + v11.fromCharCode(v4);
        fromCharCode = String;
        fromCharCode = fromCharCode.fromCharCode;
        return v8 + fromCharCode.fromCharCode(v5);
    }

    static testTypedArrays(param_0, param_1, param_2): void     {
        let v6 = Int32Array;
        let v7 = 4;
        v6 = new Int32Array();
        v7 = 0;
        v6[v7] = 10;
        v6 = int32Array;
        v7 = 1;
        v6[v7] = 20;
        v6 = int32Array;
        v7 = 2;
        v6[v7] = 30;
        v6 = int32Array;
        v7 = 3;
        v6[v7] = 40;
        let v9 = int32Array;
        let v10 = 0;
        let v8 = v9[v10];
        v9 = int32Array;
        v10 = 1;
        v7 = v8 + v9[v10];
        v8 = int32Array;
        v9 = 2;
        v6 = v7 + v8[v9];
        v7 = int32Array;
        v8 = 3;
        const v4: number = v6 + v7[v8];
        v6 = String;
        return v6(v4);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testArrayReverse(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v7: Array<unknown> = v3;
        let reverse = v7.reverse;
        reverse = v7.reverse();
        let v8 = String;
        let v9 = reverse2;
        v9 = v9[v10];
        v8 = v8(v9);
        reverse = `${v8},`;
        v8 = String;
        v8 = reverse2;
        v9 = 4;
        v8 = v8[v9];
        return reverse + v8(v8);
    }

    static testFloat64Array(param_0, param_1, param_2): void     {
        let v6 = Float64Array;
        let v7 = 3;
        v6 = new Float64Array();
        v7 = 0;
        v6[v7] = 1.5;
        v6 = float64Array;
        v7 = 1;
        v6[v7] = 2.5;
        v6 = float64Array;
        v7 = 2;
        v6[v7] = 3.5;
        let v9 = float64Array;
        let v10 = 0;
        let v8 = v9[v10];
        v9 = float64Array;
        v10 = 1;
        v7 = v8 + v9[v10];
        v8 = float64Array;
        v9 = 2;
        v6 = v7 + v8[v9];
        v7 = v6 / 3;
        v8 = 2;
        return v7.toFixed(v8);
    }

    static testFromCharCode(param_0, param_1, param_2): void     {
        let v8 = String;
        let fromCharCode = v8.fromCharCode;
        fromCharCode = v8.fromCharCode(72);
        v8 = String;
        fromCharCode = v8.fromCharCode;
        v9 = 105;
        const v5: number = fromCharCode2 + v8.fromCharCode(v9);
        fromCharCode = String;
        fromCharCode = fromCharCode.fromCharCode;
        v8 = 33;
        return v5 + fromCharCode.fromCharCode(v8);
    }

    static testSetFromArray(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = Set;
        const set = new Set(v6, v7);
        const v7 = 0;
        do {
            const v8 = v7;
            const v9 = v3;
            const v9 = set;
            let add = v9.add;
            let v10 = v3;
            const v11 = v7;
            v10 = v10[v11];
            add = v7;
            const number: number = Number(add);
            const v7: number = number + 1;
        } while (number);
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

    static testImmutableCopy(param_0, param_1, param_2): void     {
        {  }[v7] = 1;
        v6 = v4;
        v7 = "b";
        v6[v7] = 2;
        v6 = {  };
        v7 = "a";
        v6[v7] = v4[v9];
        v6 = v3;
        v7 = "b";
        v8 = v4;
        v9 = "b";
        v6[v7] = v8[v9];
        v8 = String;
        v9 = v3;
        v9 = v9[v10];
        v8 = v8(v9);
        v6 = `${v8},`;
        v8 = String;
        v8 = v3;
        v9 = "b";
        v8 = v8[v9];
        return v6 + v8(v8);
    }

    static testEpsilonCompare(param_0, param_1, param_2): void     {
        let v8 = 0.1;
        const v3: number = v8 + 0.2;
        const v4 = 0.3;
        v8 = v3;
        v8 = v8 - v4;
        if (!(v8 < 0)) {
            let v8: boolean = v8 < 0;
            const v5: boolean = v8 < 1.0E-4;
            v8 = String;
            const v9: boolean = v5;
            return v8(v9);
        }
    }

    static testMapFromEntries(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        let v9: string[] = ["/* element_0 */"];
        ["/* element_0 */"][8] = v16;
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][8] = v17;
        const v4: Array<unknown> = v8;
        v8 = Map;
        const map = new Map(v8, v9, v10, v11, v12, v13, v14, v15);
        v9 = 0;
        let v10 = v9;
        const v11 = v4;
        while (!(v10 < v11.length)) {
            const v11 = v4;
            let v12 = v9;
            let v10 = v11[v12];
            v12 = map;
            const set = v12.set;
            let v13 = v10;
            let v14 = 0;
            v13 = v13[v14];
            v14 = v10;
            const v15 = 1;
            v14 = v14[v15];
            v10 = v9;
            const number: number = Number(v10);
            let v9: number = number + 1;
            continue;
        }
    }

    static testStartsEndsWith(param_0, param_1, param_2): void     {
        const v4 = "Hello, World!";
        let v8: string = v4;
        startsWith = v8.startsWith("Hello");
        let endsWith = v8.endsWith;
        v9 = "!";
        endsWith = v4.endsWith(v9);
        v9 = String;
        v9 = v9(startsWith2);
        endsWith = `${v9},`;
        v9 = String;
        v9 = endsWith2;
        return endsWith + v9(v9);
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
        const get2 = v2;
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