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
        let v6 = v3;
        super();
        v6 = super();
        v6.code = v11;
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

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_1 = func_0;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testRethrow(param_0, param_1, param_2): void     {
        const v5 = v4;
        const v7 = v5;
        const code = v7.code;
        const v5 = v4;
        const code = v5;
        return code.message;
    }

    static testWeakMap(param_0, param_1, param_2): void     {
        const v7 = WeakMap;
        let weakMap = new WeakMap(v7);
        weakMap = new WeakMap(1, v9);
        v8 = weakMap;
        let v9 = weakMap;
        v8 = weakMap;
        v9 = weakMap;
        get = v8.get(v9);
        get = get;
    }

    static testWeakSet(param_0, param_1, param_2): void     {
        const v7 = WeakSet;
        let weakSet = new WeakSet(v7);
        weakSet = new WeakSet(2, v9);
        v8 = weakSet;
        let v9 = weakSet;
        v8 = weakSet;
        let has = v8.has;
        v9 = weakSet;
        has = v8.has(v9);
        has = String;
        v8 = has;
        return has(v8);
    }

    static toString(param_0, param_1, param_2): void     {
        const v6 = "Error(";
        const v7 = String;
        const v8 = v2;
        const code = v8.code;
        let v5: number = v6 + v7(code);
        const v4: string = `${v5}): `;
        return v4 + v11.message;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static testMapForOf(param_0, param_1, param_2): void     {
        const v8 = Map;
        const map = new Map(v8);
        let v9 = map;
        let v10 = "x";
        let v11 = 10;
        v9 = map;
        set = v9.set;
        v10 = "y";
        v11 = 20;
        v9 = map;
        let entries = v9.entries;
        entries = v9.entries();
        v9 = entries;
        next = v9.next();
        next = next;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static directionLabel(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === "UP")) {
        }
    }

    static testStringEnum(param_0, param_1, param_2): void     {
        const v3 = "UP";
        const v6: string = v3;
        if (!(v6 === "UP")) {
            return;
        }
    }

    static testCustomError(param_0, param_1, param_2): void     {
        const v7 = v4;
        let toString = v7.toString;
        toString = v7.toString();
        toString = toString;
        return toString;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
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

    static testExhaustiveEnum(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8("UP");
        let v6: string = `${v8},`;
        v8 = lex_0_1;
        v8 = "DOWN";
        let v5: number = v6 + v8(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_1;
        v6 = "RIGHT";
        return v4 + v5(v6);
    }

    static testNestedTryCatch(param_0, param_1, param_2): void     {
        const v3 = "";
        const v8 = v3;
        const v3: string = `${v8}C`;
        const v7 = undefined;
        const v7 = v3;
        const v3: string = `${v7}X`;
    }

    static testPromiseStatics(param_0, param_1, param_2): void     {
        let v7 = Promise;
        resolve = v7.resolve(42);
        v7 = Promise;
        v8 = 1;
        v8 = -v8;
        reject = v7.reject(v8);
        return "promises created";
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testErrorProperties(param_0, param_1, param_2): void     {
        const v5 = v4;
        const v7 = v5;
        const message = v7.message;
        const v8 = message;
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

    static testTryFinallyNoCatch(param_0, param_1, param_2): void     {
        throw acc;
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