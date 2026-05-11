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
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
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
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
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
            let v12 = "DecompileTest53";
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

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7.push(v13);
        return;
    }

    static isOdd(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 0)) {
            const v5: boolean = v5 === 0;
            const v6 = v3;
            v6--;
            return v5(v6);
        }
        return false;
    }

    static isEven(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 === 0)) {
            const v5: boolean = v5 === 0;
            const v6 = v3;
            v6--;
            return v5(v6);
        }
        return true;
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

    static factTail(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        if (!(v6 <= 1)) {
            const v6: boolean = v6 <= 1;
            const v7 = v3;
            v7--;
            const v8 = v4;
            v8 *= v3;
            return v6(v7, v8);
        }
        return v13;
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
        lex_0_6 = func_main_0;
        v9 = anonymous_method;
        lex_0_1 = v9;
        v9 = func_main_0;
        lex_0_2 = v9;
        v9 = onCreate;
        lex_0_5 = v9;
        v9 = onDestroy;
        lex_0_4 = v9;
        v9 = EntryAbility;
        lex_0_3 = v9;
        v9 = onBackground;
        lex_0_0 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        v11.set(ViewPU.prototype, "finalizeConstruction", factorial);
        prototype = ViewPU;
        v11 = set * undefined;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = testFactorial;
        false[v15] = "get".v16;
        prototype2.initialRender = compressString;
        prototype2.rerender = initialRender;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_43;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static factorial(param_0, param_1, param_2, param_3): void     {
        const v5 = lex_0_1;
        const v6 = v3;
        const v7 = 1;
        return v5(v6, v7);
    }

    static hasPathSum(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v21;
        let v12 = v3;
        let v12 = v3;
        let v13 = v4;
        return {  };
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testPathSum(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        const v6: Array<unknown> = [];
        v8 = lex_0_5;
        v8 = v8(v6, 0, 22);
        v8 = lex_0_5;
        v9 = v6;
        v10 = 0;
        v11 = 26;
        v8 = v8(v9, v10, v11);
        v8 = lex_0_5;
        v9 = v6;
        v10 = 0;
        v11 = 5;
        v8 = v8(v9, v10, v11);
        let v12 = String;
        v12 = v12(v8);
        v10 = `${v12},`;
        v12 = String;
        v9 = v10 + v82(v82);
        v8 = `${v9},`;
        v9 = String;
        return v8 + v9(v83);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static shortestPath(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v30;
        let v11 = Map;
        const map = new Map(v11);
        v11 = Set;
        let set = new Set(v11, v12, v13, v14);
        const v12 = map;
        set = v12.set;
        const v13 = v4;
        let v14 = 0;
        v12.set(v13, v14);
        const v6 = v4;
        let set2 = v6;
        set2 = v6;
        try {
            let set2 = 999;
            let v15 = 0;
            const v16: number = v15;
            const v17 = v3;
            let v20 = v3;
            let v21 = v15;
            let v19 = v20[v21];
            v20 = 0;
            const v16 = v19[v20];
            v20 = v3;
            v21 = v15;
            v19 = v20[v21];
            v20 = 1;
            const v17 = v19[v20];
            let v18 = 1;
            v19 = v16;
            let v21 = set2;
            let v20: number = v21 + v18;
            const v22 = map;
            let get = v22.get;
            const v23 = v17;
            v22.get(v23);
            get = v22.get(v23);
            get = get2;
            get = v20 != Boolean(get === undefined)();
            let get: number = v44 >>> {  };
            const v23 = v17;
            const v24 = v20;
            v22.set(v23, v24);
            const v16 = v15;
            const number: number = Number(v16);
            let v15: number = _number + 1;
            const v12 = 999;
            const v13 = "";
            let v14: Array<unknown> = [];
            v14 = [];
            lex_0_0 = v14;
            let v15 = map;
            const forEach = v15.forEach;
            let _number = func_53;
            v15.forEach(_number);
            v15 = 0;
            _number = v15;
            let v18 = v15;
            let _number = v17[v18];
            v18 = set;
            const has = v18.has;
            const get2 = _number;
            v18.has(get2);
            this.str_4705 = v67 > {  };
            get2.get(v20);
            let get = get2.get(v20);
            let v18 = get3;
            const length = !(v18 !== undefined);
            this.str_24586 = !(v18 < v12);
            const v12: boolean = !(v18 < v12);
            const v13 = _number;
            let _number = v15;
            const number: number = Number(_number);
            let v15: number = _number + 1;
            const v6 = v13;
        } catch (e) {
            throw acc;
        }
        let set2 = 1;
        return -set2;
    }

    static testCompress(param_0, param_1, param_2): void     {
        let v6 = lex_0_6;
        v6 = v6("aabcccccaaa");
        v6 = lex_0_6;
        v7 = "abc";
        v6 = v6(v7);
        v6 = `${v6}|`;
        return v6 + v62;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testFactorial(param_0, param_1, param_2): void     {
        let v6 = lex_0_2;
        v6 = v6(5);
        v6 = lex_0_2;
        v7 = 10;
        v6 = v6(v7);
        let v8 = String;
        v8 = v8(v6);
        v6 = `${v8},`;
        v8 = String;
        return v6 + v62(v62);
    }

    static compressString(param_0, param_1, param_2, param_3): void     {
        const v8 = v3;
        return "";
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testShortestPath(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v10;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v11;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v12;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"][6] = v3;
        const v4: Array<unknown> = v6;
        v6 = lex_0_0;
        v6 = v6(v4, "A", "D");
        v6 = String;
        return v6(v6);
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

    static testMutualRecursion(param_0, param_1, param_2): void     {
        let v8 = lex_0_4;
        v8 = v8(4);
        v8 = lex_0_4;
        v9 = 7;
        v8 = v8(v9);
        v8 = lex_0_3;
        v9 = 3;
        v8 = v8(v9);
        v8 = lex_0_3;
        v9 = 6;
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `${v14},`;
        v14 = String;
        let v11: number = v12 + v14(v14);
        let v10: string = `${v11},`;
        v11 = String;
        v9 = v10 + v11(v83);
        v8 = `${v9},`;
        v9 = String;
        v10 = v84;
        return v8 + v9(v10);
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