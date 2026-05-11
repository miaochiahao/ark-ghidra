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

    constructor(param_0, param_1, param_2)     {
        const v4 = v2;
        v4.errors = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static gcd(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v4;
        while (!(v6 !== 0)) {
            loop_0: do {
    let v6: boolean = v6 !== 0;
} while (v6 !== 0);
        }
        return v6 !== 0;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v4;
        return v10 - v6;
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

    static check(param_0, param_1, param_2, param_3, param_4): void     {
        let v8 = v2;
        const errors = v8.errors;
        v8 = v13;
        errors.push(v8);
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
        v0 = v19;
        lex_0_1 = func_main_0;
        v11 = anonymous_method;
        lex_0_2 = v11;
        let v13 = ViewPU;
        let v13 = Reflect;
        let set = v13.set;
        v13.set(ViewPU.prototype, "finalizeConstruction", anonymous_method);
        prototype = undefined;
        let v0: @system.app = @system.app;
        v13 = undefined % set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v13 = undefined % set;
        prototype = set.prototype;
        v16 = "message";
        let v17 = undefined;
        false[v18] = "get".v17;
        v16 = "message";
        v17 = undefined;
        v18 = testObjectKeys;
        false[v17] = "get".v18;
        prototype2.initialRender = testReplaceAll;
        prototype2.rerender = initialRender;
        set.getEntryName = updateStateVars;
        set = set;
        set = set;
        set = registerNamedRoute;
        v13 = func_43;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static isCoprime(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = lex_0_1;
        v7 = v7(v13, v14);
        return v7 === 1;
    }

    static isValid(param_0, param_1, param_2): void     {
        const v6 = v2;
        const errors = v6.errors;
        const length = errors.length;
        return length === 0;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testCoprime(param_0, param_1, param_2): void     {
        let v8 = lex_0_2;
        v8 = v8(14, 15);
        v8 = lex_0_2;
        v9 = 14;
        v10 = 21;
        v8 = v8(v9, v10);
        v8 = lex_0_2;
        v9 = 8;
        v10 = 27;
        v8 = v8(v9, v10);
        v8 = lex_0_2;
        v9 = 12;
        v10 = 18;
        v8 = v8(v9, v10);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        v10 = `,${v82(v82) + v12}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static getErrors(param_0, param_1, param_2): void     {
        let v6 = v2;
        const errors = v6.errors;
        v6 = "; ";
        errors.join(v6);
        return errors.join(v6);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testValidator(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v14: acc = acc;
        let check = v14.check;
        let v15 = 5;
        v15 = v15 < 3;
        v14.check(v15, "5 should be greater than 3");
        check = v14.check(v15, v16);
        check = check2.check;
        check = 10;
        check = check < 20;
        v14 = "10 should be greater than 20";
        check2.check(check, v14);
        check = check2.check(check, v14);
        check = check4.check;
        let check2 = "abc";
        let length = check2.length;
        length = length === 3;
        check2 = "abc should have length 3";
        check4.check(length, check2);
        check = check4.check(length, check2);
        check = check3.check;
        let check5 = false;
        let check4 = "forced error";
        check3.check(check5, check4);
        check3 = acc;
        check3.isValid();
        valid = check3.isValid();
        check3 = acc;
        let errors = check3.getErrors;
        check3.getErrors();
        errors = check3.getErrors();
        check5 = String;
        check4 = valid2;
        check5 = check5(check4);
        errors = `:${check52}`;
        return errors2 + errors;
    }

    static testObjectKeys(param_0, param_1, param_2): void     {
        {  }[v8] = 10;
        v7 = v3;
        v8 = "y";
        v7[v8] = 20;
        v7 = v3;
        v8 = "z";
        v7[v8] = 30;
        v8 = Object;
        v8.keys(v3);
        keys = v8.keys(v9);
        const v5 = 0;
        v8 = 0;
        do {
            let v10 = keys2;
            let v11 = v8;
            let v9 = v10[v11];
            v10 = v5;
            v11 = v3;
            const v12 = v9;
            const v5: number = v11[v12] + v10;
            v9 = v8;
            const number: number = Number(v9);
            let v8: number = _number + 1;
        } while (_number);
    }

    static testReplaceAll(param_0, param_1, param_2): void     {
        const v4 = "hello world hello universe";
        const v7: string = v4;
        v7.replaceAll("hello", "hi");
        replaceAll = v7.replaceAll(v8, v9);
        return replaceAll2;
    }

    static testReverseSort(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v6: Array<unknown> = v3;
        let sort = v6.sort;
        v6.sort(onCreate);
        let v9 = String;
        let v10: Array<unknown> = v3;
        v10 = v10[v11];
        v9 = v9(v10);
        v7 = `,${v9}`;
        v9 = String;
        v9 = v3;
        v10 = 3;
        v9 = v9[v10];
        v6 = v9(v9) + v7;
        sort = `,${v6}`;
        v6 = String;
        v7 = v3;
        v9 = 7;
        v7 = v7[v9];
        return v6(v7) + sort;
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

    static testNestedMapIter(param_0, param_1, param_2): void     {
        let v10 = Map;
        let map = new Map(v10);
        v10 = Map;
        map = new Map(v10, v11, v12, v13);
        let v11 = map2;
        let set = v11.set;
        let v12 = "math";
        let v13 = 90;
        v11.set(v12, v13);
        v11 = map2;
        set = v11.set;
        v12 = "english";
        v13 = 85;
        v11.set(v12, v13);
        set = Map;
        map = new Map(set, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24);
        v11 = map3;
        set = v11.set;
        v12 = "math";
        v13 = 78;
        v11.set(v12, v13);
        v11 = map3;
        set = v11.set;
        v12 = "english";
        v13 = 92;
        v11.set(v12, v13);
        v11 = map;
        set = v11.set;
        v12 = "alice";
        v13 = map2;
        v11.set(v12, v13);
        v11 = map;
        set = v11.set;
        v12 = "bob";
        v13 = map3;
        v11.set(v12, v13);
        set = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        const v6: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        const v8 = 0;
        const v5 = 0;
        v11 = 0;
        v12 = v11;
        const v14 = v6;
        let v15 = v11;
        let v12 = v14[v15];
        v15 = map;
        let get = v15.get;
        const v16 = v12;
        v15.get(v16);
        get = v15.get(v16);
        get = get2;
        const v16 = get2;
        let get = v16.get;
        const v17 = "math";
        v16.get(v17);
        get = v16.get(v17);
        let get3 = get;
        if (get !== undefined && get3 !== undefined) {
            let get3 = v8;
            const v8: number = get + get3;
            get3 = v5;
            const v5: number = 1 + get3;
        }
    }

    static testReduceGroupBy(param_0, param_1, param_2): void     {
        let v10: Array<unknown> = [];
        const v8: Array<unknown> = [];
        v10 = Map;
        const map = new Map(v10, v11);
        let v11 = 0;
        const v15 = v8;
        let v16 = v11;
        v16 = v15[v16];
        v16.charAt(0);
        charAt = v16.charAt(v17);
        v16 = map;
        let get = v16.get;
        v17 = charAt2;
        v16.get(v17);
        get = v16.get(v17);
        get = get2;
        if (get === undefined) {
            let v16: Array<unknown> = [];
            v14[16] = v0;
            let get: Array<unknown> = v16;
            let v17 = map;
            const set = v17.set;
            const v18 = charAt2;
            const v19: Array<unknown> = get;
            v17.set(v18, v19);
        } else {
            let isUndefined: boolean = get === undefined;
            const push = isUndefined.push;
            let v17 = v14;
            isUndefined.push(v17);
            isUndefined = map;
            const set = isUndefined.set;
            v17 = charAt2;
            const v18 = get2;
            isUndefined.set(v17, v18);
        }
        const get2 = v11;
        const number: number = Number(get2);
        let v11: number = _number + 1;
        let v10 = v4;
        const length = v10.length;
        v10 = v6;
        let v10 = v6;
        const length = v10.length;
        let _number = "a:";
        const charAt2 = String;
        const v14 = length;
        let v11: number = charAt2(v14) + _number;
        v10 = `,b:${v11}`;
        v11 = String;
        _number = length2;
        return v11(_number) + v10;
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