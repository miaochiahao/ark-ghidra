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

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        v2.label = v10;
        v6 = v2;
        v6.value = v11;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v11 + v6;
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

    static format(param_0, param_1, param_2): void     {
        let v6 = v2;
        let label = v6.label;
        const v4: string = `=${label}`;
        label = String;
        const value = v6.value;
        return label(value) + v4;
    }

    static testReduce(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v7: Array<unknown> = v3;
        let reduce = v7.reduce;
        reduce = v7.reduce(func_3, 0);
        return reduce;
    }

    static testSetOps(param_0, param_1, param_2): void     {
        const v5 = Set;
        const set = new Set(v5);
        let v6 = set;
        let add = v6.add;
        let v7 = 1;
        v6 = set;
        add = v6.add;
        v7 = 2;
        v6 = set;
        add = v6.add;
        v7 = 2;
        v6 = set;
        add = v6.add;
        v7 = 3;
        add = set;
        return add.size;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testNullish(param_0, param_1, param_2, param_3, param_4): void     { }

    static testTernary(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 < 0)) {
        }
    }

    static getValue(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.value;
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

    static testSplitJoin(param_0, param_1, param_2): void     {
        const v3 = "apple,banana,cherry";
        let v7: string = v3;
        let split = v7.split;
        split = v7.split(",");
        v7 = split;
        v8 = "|";
        return v7.join(v8);
    }

    static testInt32Array(param_0, param_1, param_2): void     {
        let v7 = 4;
        v6 = new 4(v6);
        v7 = 0;
        v6[10] = v7;
        v6 = v3;
        v7 = 1;
        v6[20] = v7;
        v6 = v3;
        v7 = 2;
        v6[30] = v7;
        v6 = v3;
        v7 = 3;
        v6[40] = v7;
        const v4 = 0;
        v7 = 0;
        const v9 = v3;
        while (!(v9.length < v8)) {
            let v8 = v4;
            const v9 = v3;
            const v10 = v7;
            const v4: number = v9[v10] + v8;
            v8 = v7;
            const number: number = Number(v8);
            let v7: number = number + 1;
            continue;
        }
    }

    static get getEntryName(): void     {
        return "Index";
    }

    static testMapIteration(param_0, param_1, param_2): void     {
        const v5 = Map;
        const map = new Map(v5);
        let v6 = map;
        let set = v6.set;
        let v7 = "a";
        let v8 = 1;
        v6 = map;
        set = v6.set;
        v7 = "b";
        v8 = 2;
        v6 = map;
        set = v6.set;
        v7 = "c";
        v8 = 3;
        set = [];
        set = [];
        lex_0_0 = set;
        v6 = map;
        v7 = func_18;
        v7 = ",";
        return v6.join(v7);
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

    static testInterfaceImpl(param_0, param_1, param_2): void     {
        let v7 = "score";
        v7 = new 100();
        return v7.format();
    }

    static testOptionalChain(param_0, param_1, param_2, param_3): void     {
        const v7 = v3;
    }

    static testTypeAssertion(param_0, param_1, param_2, param_3): void     {
        const v4 = v3;
        const v7 = v4;
        return v7.toUpperCase();
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

    static testFloat64ArrayFrom(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v7 = Float64Array;
        let from = v7.from;
        from = v7.from(v3);
        v8 = from;
        v7 = v8[v9];
        v8 = from;
        v9 = 1;
        from = v8[v9] + v7;
        v7 = from;
        v8 = 2;
        return v7[v8] + from;
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