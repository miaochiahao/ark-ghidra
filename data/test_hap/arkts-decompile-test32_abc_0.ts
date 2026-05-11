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
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v10 = "testTag";
        const v11 = "Failed to set colorMode. Cause: %{public}s";
        const v13 = JSON;
        v13.stringify(v6);
        stringify = v13.stringify(v14);
        v8.error(v9, v10, v11);
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

    constructor(param_0, param_1, param_2, param_3)     {
        v2 = v8;
        v2.count = 0;
        v5 = v2;
        v5.step = v9;
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
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

    static compare(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v6 - v11;
    }

    static reset(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.count = 0;
        return;
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
        lex_0_1 = func_main_0;
        v12 = anonymous_method;
        lex_0_0 = v12;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static getValue(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.count;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static decrement(param_0, param_1, param_2): void     {
        const v4 = v2;
        let v6 = v2;
        const count = v6.count;
        v4.count = count - v9.step;
        return;
    }

    static increment(param_0, param_1, param_2): void     {
        const v4 = v2;
        let v6 = v2;
        const count = v6.count;
        v4.count = count + v9.step;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testNestedMap(param_0, param_1, param_2): void     {
        let v9 = Map;
        let map = new Map(v9);
        v9 = Map;
        map = new Map(v9, v10, v11, v12);
        let v10 = map2;
        let set = v10.set;
        let v11 = "x";
        let v12 = 10;
        v10.set(v11, v12);
        v10 = map2;
        set = v10.set;
        v11 = "y";
        v12 = 20;
        v10.set(v11, v12);
        set = Map;
        map = new Map(set, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23);
        v10 = map3;
        set = v10.set;
        v11 = "x";
        v12 = 30;
        v10.set(v11, v12);
        v10 = map3;
        set = v10.set;
        v11 = "y";
        v12 = 40;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "first";
        v12 = map2;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "second";
        v12 = map3;
        v10.set(v11, v12);
        set = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        const v5: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        v10 = 0;
    }

    static httpStatusText(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 200)) {
            const v5: boolean = v5 === 200;
            if (!(v5 === 301)) {
                const v5: boolean = v5 === 301;
                if (!(v5 === 404)) {
                    const v5: boolean = v5 === 404;
                    return "Server Error";
                }
                return "Not Found";
            }
            return "Moved";
        }
        return "OK";
    }

    static testSortReverse(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v8: Array<unknown> = v3;
        v8.sort(lex_0_1);
        sort = v8.sort(v9);
        v8 = sort2;
        let reverse = v8.reverse;
        v8.reverse();
        reverse = v8.reverse();
        let v11 = String;
        let v12 = reverse2;
        v12 = v12[v13];
        v11 = v11(v12);
        v9 = `${v11},`;
        v11 = String;
        v11 = reverse2;
        v12 = 1;
        v11 = v11[v12];
        v8 = v9 + v11(v11);
        reverse = `${v8},`;
        v8 = String;
        v9 = reverse2;
        v11 = 5;
        v9 = v9[v11];
        return reverse + v8(v9);
    }

    static testStepCounter(param_0, param_1, param_2): void     {
        let v8 = 5;
        v8 = new 5();
        v8.increment();
        v8 = v3;
        increment = v8.increment;
        v8.increment();
        v8 = v3;
        increment = v8.increment;
        v8.increment();
        v8 = v3;
        v8.decrement();
        v8 = v3;
        let value = v8.getValue;
        v8.getValue();
        value = v8.getValue();
        v3.reset();
        value = v3.getValue;
        v3.getValue();
        value = v3.getValue();
        let v9 = String;
        v9 = v9(value2);
        value = `${v9},`;
        v9 = String;
        v9 = value3;
        return value + v9(v9);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testNumberChecks(param_0, param_1, param_2): void     {
        let v9 = Number;
        v9.isFinite(42);
        finite = v9.isFinite(v10);
        v9 = Number;
        finite = v9.isFinite;
        v10 = Infinity;
        v9.isFinite(v10);
        finite = v9.isFinite(v10);
        v9 = Number;
        let naN = v9.isNaN;
        v10 = NaN;
        v9.isNaN(v10);
        naN = v9.isNaN(v10);
        v9 = Number;
        naN = v9.isNaN;
        v10 = 0;
        v9.isNaN(v10);
        naN = v9.isNaN(v10);
        let v14 = String;
        v14 = v14(finite2);
        let v12: string = `${v14},`;
        v14 = String;
        v14 = finite3;
        v10 = `${v12 + v14(v14)},`;
        v11 = String;
        v12 = naN2;
        v9 = v10 + v11(v12);
        naN = `${v9},`;
        v9 = String;
        v10 = naN3;
        return naN + v9(v10);
    }

    static testSwitchReturn(param_0, param_1, param_2): void     {
        let v8 = lex_0_0;
        v8 = v8(200);
        let v6: string = `${v8},`;
        v8 = lex_0_0;
        v8 = 404;
        let v5: number = v6 + v8(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_0;
        v6 = 999;
        return v4 + v5(v6);
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

    static testChainedTransform(param_0, param_1, param_2): void     {
        const v4 = "  Hello, WORLD!  ";
        const v11: string = v4;
        v11.trim();
        trim = v11.trim();
        let toLowerCase = trim2.toLowerCase;
        trim2.toLowerCase();
        toLowerCase = trim2.toLowerCase();
        toLowerCase = "world";
        const trim2 = "arkts";
        toLowerCase2.replace(toLowerCase, trim2);
        replace = toLowerCase2.replace(toLowerCase, trim2);
        return replace2;
    }

    static testObjectAccumulate(param_0, param_1, param_2): void     {
        {  }[v9] = 85;
        v8 = v5;
        v9 = "bob";
        v8[v9] = 92;
        v8 = v5;
        v9 = "carol";
        v8[v9] = 78;
        v9 = Object;
        v9.keys(v5);
        keys = v9.keys(v10);
        v9 = 0;
        const v10 = v9;
        const v11 = keys2;
        while (!(v10 < v11.length)) {
            loop_0: do {
    let hasLength: boolean = v10 < v11.length;
    let v11 = keys2;
} while (hasLength < v11.length);
        }
    }

    static testStringTrimRepeat(param_0, param_1, param_2): void     {
        const v6 = "  hello  ";
        let v10: string = v6;
        v10.trim();
        trim = v10.trim();
        v10 = v6;
        v10.trimStart();
        trimStart = v10.trimStart();
        v10 = v6;
        v10.trimEnd();
        trimEnd = v10.trimEnd();
        v10 = "ab";
        let repeat = v10.repeat;
        v10.repeat(3);
        repeat = v10.repeat(v11);
        v11 = `${`${trim2}|` + trimStart2}|`;
        v10 = v11 + trimEnd2;
        repeat = `${v10}|`;
        return repeat + repeat2;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
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