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

    static testNumberParse(param_0, param_1, param_2): void     {
        const v6 = "42";
        const v4 = "3.14";
        let v9 = parseInt;
        v9 = v9(v6, 10);
        v9 = parseFloat;
        v9 = v9(v4);
        let v12 = v9;
        let toFixed = v12.toFixed;
        toFixed = v12.toFixed(0);
        v9 = `${toFixed},`;
        toFixed = v9;
        toFixed = toFixed.toFixed;
        v12 = 2;
        return v9 + toFixed.toFixed(v12);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testMathChaining(param_0, param_1, param_2): void     {
        const v9 = 3.7;
        const v7: number = -v9;
        let v10 = Math;
        let abs = v10.abs;
        abs = v10.abs(v7);
        v10 = Math;
        let ceil = v10.ceil;
        v11 = abs;
        ceil = v10.ceil(v11);
        v10 = Math;
        let floor = v10.floor;
        v11 = abs;
        floor = v10.floor(v11);
        v10 = Math;
        let round = v10.round;
        v11 = v7;
        round = v10.round(v11);
        let v13 = String;
        v13 = v13(ceil);
        v11 = `${v13},`;
        v13 = String;
        v13 = floor;
        v10 = v11 + v13(v13);
        round = `${v10},`;
        v10 = String;
        v11 = round;
        return round + v10(v11);
    }

    static testMultiVarDecl(param_0, param_1, param_2): void     {
        const v3 = 1;
        const v4 = 2;
        const v5 = 3;
        let v10: number = v3;
        let v9: number = v10 + v4;
        const v7: number = v9 + v5;
        v9 = v3 * v4;
        const v6: number = v9 * v5;
        let v11 = String;
        v11 = v11(v7);
        v9 = `${v11},`;
        v11 = String;
        return v9 + v6(v6);
    }

    static testNestedRecord(param_0, param_1, param_2): void     {
        {  }[v7] = {  };
        v7 = v3;
        v6 = v7[v8];
        v7 = "col1";
        v6[v7] = 10;
        v7 = v3;
        v8 = "row1";
        v6 = v7[v8];
        v7 = "col2";
        v6[v7] = 20;
        v6 = v3;
        v7 = "row1";
        const v4 = v6[v7];
        v8 = String;
        let v9 = v4;
        v9 = v9[v10];
        v8 = v8(v9);
        v6 = `${v8},`;
        v8 = String;
        v8 = v4;
        v9 = "col2";
        v8 = v8[v9];
        return v6 + v8(v8);
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

    static testArrayIsNarrow(param_0, param_1, param_2): void     {
        const v5: Array<unknown> = [];
        const v8 = Array;
        let array = v8.isArray;
        array = v8.isArray(v5);
        const v4 = 0;
        v9 = String;
        v9 = v9(array);
        array = `${v9},`;
        v9 = String;
        return array + v4(v4);
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

    static testStringRepeatTrim(param_0, param_1, param_2): void     {
        const v4 = " ha ";
        let v8: string = v4;
        let repeat = v8.repeat;
        repeat = v8.repeat(3);
        v8 = repeat;
        let trim = v8.trim;
        trim = v8.trim();
        return trim;
    }

    static testTemplateWithExpr(param_0, param_1, param_2): void     {
        const v3 = 10;
        const v4 = 20;
        let v11 = String;
        const v12: number = v3;
        v12 += v4;
        v11 = v11(v12);
        let v9: string = `${v11}=`;
        v11 = String;
        let v8: number = v9 + v11(v11);
        const v7: string = `${v8}+`;
        v8 = String;
        return v7 + v8(v4);
    }

    static testShortCircuitAssign(param_0, param_1, param_2): void     {
        const v4 = 5;
    }

    static testObjectEntriesValues(param_0, param_1, param_2): void     {
        {  }[v10] = 1;
        v9 = v5;
        v10 = "b";
        v9[v10] = 2;
        v9 = v5;
        v10 = "c";
        v9[v10] = 3;
        v10 = Object;
        let keys = v10.keys;
        keys = v10.keys(v5);
        keys = [];
        const v7: Array<unknown> = [];
        const v4 = 0;
        do {
            let v10 = keys;
            let v10 = keys;
            let v11 = v4;
            let keys = v10[v11];
            v11 = v7;
            let push = v11.push;
            let v12 = v5;
            const v13 = keys;
            v12 = v12[v13];
            push = v4;
            const v4: number = push + 1;
        } while (push + 1);
        do {
            let v12 = v7;
            let v11 = v6;
            let v12 = v7;
            const v13 = push;
            const v6: number = v11 + v12[v13];
            v11 = push;
            const number: number = Number(v11);
            let push: number = number + 1;
        } while (number);
    }

    static testChainedTernaryLookup(param_0, param_1, param_2): void     {
        const v4 = 2;
        const v6: number = v4;
        if (v6 === 0) {
            const v3 = "read";
        }
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