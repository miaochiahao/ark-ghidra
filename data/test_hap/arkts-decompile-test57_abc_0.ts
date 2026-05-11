export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v8 = "testTag";
        const v9 = "Failed to load the content. Cause: %{public}s";
        const v11 = JSON;
        v11.stringify(v16);
        stringify = v11.stringify(v12);
        v6.error(v7, v8, v9);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        try {
        } catch (e) {
            const error = v8.error;
            const v10 = "testTag";
            const v11 = "Failed to set colorMode. Cause: %{public}s";
            const v13 = JSON;
            let stringify = v13.stringify;
            const v14 = v6;
            v13.stringify(v14);
            stringify = v13.stringify(v14);
            v8.error(v9, v10, v11);
        }
        const v9 = "testTag";
        const v10 = "%{public}s";
        const v11 = "Ability onCreate";
        error.info(v8, v9, v10);
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v9 = "%{public}s";
        v6.info(v7, v8, v9);
        const v6 = v3;
        const v7 = "pages/Index";
        v8 = onBackground;
        v6.loadContent(v7, v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        const v7 = "testTag";
        const v8 = "%{public}s";
        v5.info(v6, v7, v8);
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        try {
        } catch (e) {
            throw v2;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_0 = 0;
        v4 = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static async onBackup(param_0, param_1, param_2): void     {
        try {
            throw v5;
        } catch (e) {
            throw v3;
        }
    }

    static async onRestore(param_0, param_1, param_2, param_3): void     {
        try {
            throw v6;
        } catch (e) {
            throw v4;
        }
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
            const v10 = v7;
            if (v10 === undefined) {
                v7 = undefined;
            }
            const v11 = ObservedPropertySimplePU;
            let v12 = "DecompileTest57";
            const v14 = "message";
            v10.__message = new ObservedPropertySimplePU(v12, v13, v14, v15, v16, v17, v18, v19, v20, v21);
            const initiallyProvidedValue = v11.setInitiallyProvidedValue;
            v12 = v4;
            v11.setInitiallyProvidedValue(v12);
            const finalizeConstruction = v11.finalizeConstruction;
            v11.finalizeConstruction();
        }
        let isUndefined: boolean = v10 === undefined;
        const v11 = v5;
        let v12 = v6;
        const v13 = v8;
        super(isUndefined, v11, v12);
        isUndefined = super(isUndefined, v11, v12);
        v2 = isUndefined;
        isUndefined = typeof v7;
        isUndefined.paramsGenerator_ = v7;
        try {
        } catch (e) {
            throw v2;
        }
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        v4++;
        v4 = v4;
        lex_0_0 = v4;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_0;
        let v6 = lex_0_1;
        v6 = v6(v11);
        return v5(v6);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 > 3;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let get = v8.get;
        v8.get(v14);
        get = v8.get(v9);
        get = get2;
        if (!(get !== undefined)) {
            const isUndefined: boolean = get !== undefined;
            const v8 = v3;
            const undefined = isUndefined(v8);
            const set = v8.set;
            const v9 = v3;
            const v10 = _undefined;
            v8.set(v9, v10);
            return _undefined;
        }
        return get2;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_0;
        return v5 + v9;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * v9;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v6 + v11;
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
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static compose(param_0, param_1, param_2, param_3, param_4): void     {
        lex_0_0 = v9;
        lex_0_1 = v10;
        return func_main_0;
    }

    static memoize(param_0, param_1, param_2, param_3): void     {
        lex_0_1 = v9;
        const v5 = Map;
        let map = new Map(v5);
        map = map;
        return onCreate;
    }

    static curryAdd(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v8;
        return func_main_0;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        v7.create(lex_0_0.message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        lex_0_2 = func_main_0;
        v9 = anonymous_method;
        lex_0_1 = v9;
        v9 = func_main_0;
        lex_0_0 = v9;
        v9 = onCreate;
        lex_0_7 = v9;
        v9 = onDestroy;
        lex_0_3 = v9;
        v9 = EntryAbility;
        lex_0_5 = v9;
        v9 = onBackground;
        lex_0_4 = v9;
        v9 = onForeground;
        lex_0_6 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", anonymous_method);
        prototype = ViewPU;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = slowSquare;
        false[v15] = "get".v16;
        prototype2.initialRender = message;
        prototype2.rerender = testMemoize;
        set.getEntryName = createCounter;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = testProcessNumbers;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static testCurry(param_0, param_1, param_2): void     {
        let v8 = lex_0_7;
        v8 = v8(5);
        v8 = lex_0_7;
        v9 = 10;
        v8 = v8(v9);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        v8 = v82;
        v9 = 7;
        v8 = v8(v9);
        let v10 = String;
        v10 = v10(v83);
        v8 = `${v10},`;
        v10 = String;
        return v8 + v84(v84);
    }

    static slowSquare(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 * v9;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testCompose(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8(lex_0_2, lex_0_3);
        v8 = lex_0_1;
        v9 = lex_0_3;
        v10 = lex_0_2;
        v8 = v8(v9, v10);
        v8 = v8;
        v9 = 3;
        v8 = v8(v9);
        v8 = v82;
        v9 = 3;
        v8 = v8(v9);
        v10 = String;
        v10 = v10(v83);
        v8 = `${v10},`;
        v10 = String;
        return v8 + v84(v84);
    }

    static testCounter(param_0, param_1, param_2): void     {
        let v10 = lex_0_0;
        v10 = v10(0);
        v10 = lex_0_0;
        v11 = 10;
        v10 = v10(v11);
        v10 = v10;
        v10 = v10();
        v10 = v10;
        v10 = v10();
        v10 = v102;
        v10 = v10();
        v10 = v102;
        v10 = v10();
        let v16 = String;
        v16 = v16(v103);
        let v14: string = `${v16},`;
        v16 = String;
        let v13: number = v14 + v16(v16);
        let v12: string = `${v13},`;
        v13 = String;
        v11 = v12 + v13(v105);
        v10 = `${v11},`;
        v11 = String;
        v12 = v106;
        return v10 + v11(v12);
    }

    static testMemoize(param_0, param_1, param_2): void     {
        let v8 = lex_0_5;
        v8 = v8(lex_0_6);
        v8 = v8;
        v9 = 4;
        v8 = v8(v9);
        v8 = v8;
        v9 = 4;
        v8 = v8(v9);
        v8 = v8;
        v9 = 5;
        v8 = v8(v9);
        let v12 = String;
        v12 = v12(v82);
        let v10: string = `${v12},`;
        v12 = String;
        v9 = v10 + v83(v83);
        v8 = `${v9},`;
        v9 = String;
        return v8 + v9(v84);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static createCounter(param_0, param_1, param_2, param_3): void     {
        lex_0_0 = v9;
        return func_main_0;
    }

    static processNumbers(param_0, param_1, param_2, param_3): void     {
        let v9 = v3;
        v9.filter(func_main_0);
        filter = v9.filter(v10);
        v9 = filter2;
        v10 = onForeground;
        v9.map(v10);
        map = v9.map(v10);
        v9 = map2;
        let reduce = v9.reduce;
        v10 = onRestore;
        v9.reduce(v10, 0);
        reduce = v9.reduce(v10, v11);
        reduce = String;
        v9 = reduce2;
        return reduce(v9);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5.observeComponentCreation2(func_main_0, Column);
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = onWindowStageCreate;
        v7 = Text;
        v5.observeComponentCreation2(v6, v7);
        v5 = Text;
        v5.pop();
        v5 = Column;
        pop = v5.pop;
        v5.pop();
        return;
    }

    static testProcessNumbers(param_0, param_1, param_2): void     {
        const v4 = lex_0_4;
        let v5: Array<unknown> = [];
        v5 = [];
        return v4(v5);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.aboutToBeDeleted();
        let v7 = SubscriberManager;
        v7.Get();
        get = v7.Get();
        let id__ = v7.id__;
        v10.id__();
        id__ = v10.id__();
        get2.delete(id__);
        const get2 = v2;
        get2.aboutToBeDeletedInternal();
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const message = v6.message;
        const message = v2;
        const v6 = v3;
        message.message = v6.message;
        return;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.purgeDependencyOnElmtId(v11);
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