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
        {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            v6.info(v7, v8, v9);
            return;
        }
        const v11 = JSON;
        const v12 = v3;
        v11.stringify(v12);
        stringify = v11.stringify(v12);
        v6.error(v7, "testTag", "Failed to load the content. Cause: %{public}s");
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        let v4 = 0;
        lex_0_0 = v4;
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
        const v11 = "Ability onCreate";
        error.info(v8, "testTag", "%{public}s");
        return;
    }

    static onDestroy(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        v6.info(v7, v8, "%{public}s");
        const v6 = v3;
        v8 = onBackground;
        v6.loadContent("pages/Index", v8);
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        v5.info(v6, "testTag", "%{public}s");
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
        let v4 = 0;
        lex_0_0 = v4;
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
        if (v10 === undefined) {
            const v10 = 1;
            v6 = -v10;
            const v10 = v7;
            if (v10 === undefined) {
                v7 = undefined;
            }
            const v11 = ObservedPropertySimplePU;
            let v12 = "DecompileTest61";
            const v14 = "message";
            v10.__message = new ObservedPropertySimplePU(v12, v13, v14, v15, v16, v17, v18, v19, v20, v21);
            const initiallyProvidedValue = v11.setInitiallyProvidedValue;
            v12 = v4;
            v11.setInitiallyProvidedValue(v12);
            const finalizeConstruction = v11.finalizeConstruction;
            v11.finalizeConstruction();
        }
        let isUndefined: boolean = v6 === undefined;
        const v13 = v8;
        super(isUndefined, v11, v12);
        isUndefined = super(isUndefined, v20, v6);
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

    static twoSum(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = 0;
        const v9: number = v8;
        let v12 = v3;
        let v13 = v8;
        v12 = new Map(Map);
        let get = v12.get;
        v13 = v19 - v12[v13];
        v12.get(v13);
        get = v12.get(v13);
        get = get2;
        if (get === undefined) {
            const isUndefined: boolean = get !== undefined;
            const set = isUndefined.set;
            let v13 = v3;
            let v14 = v8;
            v13 = v13[v14];
            v14 = v8;
            isUndefined.set(v13, v14);
            const v9 = v8;
            const number: number = Number(v9);
            const v8: number = _number + 1;
        }
        get2[11] = v15;
        v8[11] = v16;
        return [];
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Column;
        v7.create();
        v7 = Column;
        let v8 = "100%";
        v7.width(v8);
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static testDFS(param_0, param_1, param_2): void     {
        const map = new Map(Map);
        let v7 = map;
        let v8 = "A";
        let v9: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = "B";
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = "C";
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = "D";
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = "E";
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */"];
        v7.set(v8, v9);
        v7 = map;
        set = v7.set;
        v8 = "F";
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */"];
        v9 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */"];
        v7.set(v8, v9);
        set = lex_0_1;
        v7 = map;
        v8 = "A";
        set = set(v7, v8);
        v7 = set2;
        v8 = ",";
        v7.join(v8);
        return v7.join(v8);
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = Text;
        let message = lex_0_0.message;
        v7.create(message);
        v7 = Text;
        message = 30;
        v7.fontSize(message);
        v7 = Text;
        message = FontWeight;
        v7.fontWeight(message.Bold);
        return;
    }

    static func_main_0(param_0, param_1, param_2): void     {
        let v9 = func_main_0;
        lex_0_2 = v9;
        v9 = anonymous_method;
        lex_0_4 = v9;
        v9 = func_main_0;
        lex_0_0 = v9;
        v9 = onCreate;
        lex_0_1 = v9;
        v9 = onDestroy;
        lex_0_3 = v9;
        v9 = EntryAbility;
        lex_0_5 = v9;
        v9 = onBackup;
        lex_0_6 = v9;
        let v11 = ViewPU;
        let v11 = Reflect;
        let set = v11.set;
        let prototype = ViewPU.prototype;
        let v14 = testTwoSum;
        v11.set(prototype, "finalizeConstruction", v14);
        prototype = ViewPU;
        set = @system.app;
        v11 = @system.app;
        prototype = set.prototype;
        v14 = "message";
        let v15 = undefined;
        false[v16] = "get".v15;
        v14 = "message";
        v15 = undefined;
        v16 = testNextGreater;
        false[v15] = "get".v16;
        prototype2.initialRender = getEntryName;
        prototype2.rerender = nextGreaterElement;
        set.getEntryName = setInitiallyProvidedValue;
        set = set;
        set = set;
        set = registerNamedRoute;
        v11 = func_43;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static dfsHelper(param_0, param_1, param_2, param_3, param_4, param_5, param_6): void     {
        v5 = v22;
        v6 = v23;
        let v10 = v5;
        let v11 = v4;
        v10.add(v11);
        v10 = v6;
        v11 = v21;
        v10.push(v11);
        v10 = v20;
        v11 = v21;
        v10.get(v11);
        get = v10.get(v11);
        get = get2;
        let v10 = 0;
        do {
            const v12 = get2;
            let v13 = v10;
            let v11 = v12[v13];
            v13 = v5;
            const has = v13.has;
            const v14 = v11;
            v13.has(v14);
        } while (true);
        let v13 = v3;
        const v14 = v11;
        const number: number = Number(v10);
        let v10: number = _number + 1;
    }

    static coinChange(param_0, param_1, param_2, param_3, param_4): void     {
        const v7: Array<unknown> = [];
        const v5: Array<unknown> = [];
        let v8 = 0;
        const v9 = v8;
        while (v9 > v4) {
            loop_0: do {
    let v9: boolean = v9 <= v4;
} while (v9 <= v4);
        }
        const v7: boolean = v9 <= v4;
        let v8 = 0;
        v7[v8] = 0;
        v8 = 1;
        const v9: number = v8;
        const v10 = 0;
        const v11: number = v10;
        let v12 = v3;
        let v12 = v3;
        let v13 = v10;
        const v11 = v12[v13];
        let v13 = v5;
        let v14: number = v8 - v3[v17];
        let v12 = v13[v14];
        v12 = v12 + 1;
        v13 = v5;
        v14 = v8;
        if (v11 <= v8 && v12 < v13[v14]) {
            let v12 = v5;
            let v13 = v8;
            v12[v13] = v11;
        }
        const v7 = 1;
        return -v7;
    }

    static testTwoSum(param_0, param_1, param_2): void     {
        let v6 = lex_0_6;
        let v7: Array<unknown> = [];
        v7 = [];
        let v8 = 9;
        v6 = v6(v7, v8);
        v6 = lex_0_6;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v8 = 6;
        v6 = v6(v7, v8);
        let v9 = v6;
        let join = v9.join;
        const v10 = ",";
        v9.join(v10);
        join = v9.join(v10);
        v6 = `${join2}|`;
        join = v62;
        join = join.join;
        v9 = ",";
        join.join(v9);
        return v6 + join.join(v9);
    }

    static message(param_0, param_1, param_2): void     {
        const __message = v2.__message;
        __message.get();
        return __message.get();
    }

    static testSubsets(param_0, param_1, param_2): void     {
        let v5 = lex_0_3;
        let v6: Array<unknown> = [];
        v6 = [];
        v5 = v5(v6);
        v5 = String;
        const length = v6.length;
        return v5(length);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static dfsTraversal(param_0, param_1, param_2, param_3, param_4): void     {
        v8 = [];
        const v5: Array<unknown> = [];
        v8 = lex_0_0;
        return v5;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testCoinChange(param_0, param_1, param_2): void     {
        let v7 = lex_0_4;
        let v8: Array<unknown> = [];
        v8 = [];
        let v9 = 11;
        v7 = v7(v8, v9);
        v7 = lex_0_4;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */"];
        v9 = 3;
        v7 = v7(v8, v9);
        v7 = lex_0_4;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */"];
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */"];
        v9 = 0;
        v7 = v7(v8, v9);
        let v11 = String;
        v11 = v11(v7);
        v9 = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static generateSubsets(param_0, param_1, param_2, param_3): void     {
        let v7: Array<unknown> = [];
        const v5: Array<unknown> = [];
        v7 = [];
        v7 = lex_0_2;
        return v5;
    }

    static testNextGreater(param_0, param_1, param_2): void     {
        let v5 = lex_0_5;
        let v6: Array<unknown> = [];
        v6 = [];
        v5 = v5(v6);
        const v7 = ",";
        v5.join(v7);
        return v5.join(v7);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static backtrackSubsets(param_0, param_1, param_2, param_3, param_4, param_5, param_6): void     {
        v5 = v18;
        v6 = v19;
        const v8 = v4;
        let v9 = v3;
        if (v8 !== v9.length) {
            const hasLength: boolean = v8 === v9.length;
            let v9 = v3;
            let v10 = v4;
            v10++;
            let v11 = v5;
            let v12 = v6;
            v9 = v5;
            let push = v9.push;
            v10 = v3;
            v11 = v4;
            v10 = v10[v11];
            v9.push(v10);
            push = lex_0_2;
            v9 = v3;
            v10 = v4;
            v10++;
            v11 = v5;
            v12 = v6;
            v9 = v5;
            const pop = v9.pop;
            v9.pop();
            return;
        }
        let v10 = v5;
        let v11 = 0;
        v10.slice(v11);
        slice = v10.slice(v11);
        v10 = v6;
        let push = v10.push;
        v11 = slice2;
        v10.push(v11);
        return;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        let v6 = func_main_0;
        let v7 = Column;
        v5.observeComponentCreation2(v6, v7);
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

    static nextGreaterElement(param_0, param_1, param_2, param_3): void     {
        let v7: Array<unknown> = [];
        v7 = [];
        const v5: Array<unknown> = [];
        const v8 = 0;
        do {
            let v10 = v4;
            let push = v10.push;
            let v11 = 1;
            v11 = -v11;
            v10.push(v11);
            push = v8;
            const number: number = Number(push);
            const v8: number = _number + 1;
        } while (_number);
        let v10 = v5;
        v10 = v19;
        let v11 = v8;
        length = v10[v11];
        let v12 = v5;
        length = v5.length;
        let v13: number = length2 - 1;
        v11 = v12[v13];
        v11.pop();
        pop = v11.pop();
        pop = [];
        v11 = pop2;
        v12 = v19;
        v13 = v8;
        pop[v11] = v12[v13];
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const __message = v2.__message;
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
        v2 = v9;
        v2.message = v10.message;
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