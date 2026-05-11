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
            let v12 = "DecompileTest44";
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

    static addOne(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return 1 + v5;
    }

    static double(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return 2 * v5;
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
        lex_0_0 = v10;
        lex_0_1 = v11;
        return func_main_0;
    }

    static inRange(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v14;
        const v8 = v3;
        const v8 = v3;
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
        lex_0_4 = func_main_0;
        v11 = anonymous_method;
        lex_0_2 = v11;
        v11 = func_main_0;
        lex_0_1 = v11;
        v11 = onCreate;
        lex_0_3 = v11;
        v11 = onDestroy;
        lex_0_0 = v11;
        v11 = EntryAbility;
        lex_0_6 = v11;
        v11 = onBackground;
        lex_0_7 = v11;
        v11 = onForeground;
        lex_0_5 = v11;
        let v13 = ViewPU;
        let v13 = Reflect;
        let set = v13.set;
        v13.set(ViewPU.prototype, "finalizeConstruction", testCompose);
        prototype = ViewPU;
        v13 = undefined % set;
        prototype = set.prototype;
        v16 = "message";
        let v17 = undefined;
        false[v18] = "get".v17;
        v16 = "message";
        v17 = undefined;
        v18 = anonymous_method;
        false[v17] = "get".v18;
        prototype2.initialRender = testMapForEach;
        prototype2.rerender = testReverseWords;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        v13 = func_46;
        prototype = "";
        let prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static isLeapYear(param_0, param_1, param_2, param_3): void     {
        let v9 = v3;
        let v8: number = 4 % v9;
        const v5: boolean = v8 === 0;
        v9 = v13;
        v8 = 100 % v9;
        const v4: boolean = v8 === 0;
        v8 = 400 % v13;
        const v6: boolean = v8 === 0;
        v8 = {  };
        return new {  }(v12, v13, v4, v5, v6, v7, v8, v13, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36, v37, v38, v39, v40, v41, v42, v43, v44, v45, v46, v47, v48, v49, v50, v51, v52, v53, v54, v55, v56, v57, v58, v59, v60, v61, v62, v63, v64, v65, v66, v67, v68, v69, v70, v71, v72, v73, v74, v75, v76, v77, v78, v79, v80, v81, v82);
    }

    static testMatrix(param_0, param_1, param_2): void     {
        let v5 = lex_0_1;
        v5 = v5(3, 4, 7);
        let v9 = String;
        v9 = v9(v5.length);
        v7 = `x${v9}`;
        v9 = String;
        length = v5;
        v9 = length[v11];
        length = v9.length;
        v6 = v9(length2) + v7;
        v5 = `:${v6}`;
        v6 = String;
        v9 = v5;
        const length2 = 1;
        v7 = v9[length2];
        v9 = 2;
        v7 = v7[v9];
        return v6(v7) + v5;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testCompose(param_0, param_1, param_2): void     {
        let v8 = lex_0_2;
        v8 = v8(lex_0_3, lex_0_4);
        v8 = lex_0_2;
        v9 = lex_0_4;
        v10 = lex_0_3;
        v8 = v8(v9, v10);
        v8 = v8;
        v9 = 5;
        v8 = v8(v9);
        v8 = v82;
        v9 = 5;
        v8 = v8(v9);
        v10 = String;
        v10 = v10(v83);
        v8 = `,${v10}`;
        v10 = String;
        return v84(v84) + v8;
    }

    static testInRange(param_0, param_1, param_2): void     {
        let v8 = lex_0_0;
        v8 = v8(5, 1, 10);
        v8 = lex_0_0;
        v9 = 0;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        v8 = lex_0_0;
        v9 = 15;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        v8 = lex_0_0;
        v9 = 1;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        v11 = v82(v82) + v12;
        v10 = `,${v11}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static composed(param_0, param_1, param_2, param_3): void     {
        let v6 = lex_0_0;
        v6 = v6(v11);
        v6 = lex_0_1;
        return v6(v6);
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static createMatrix(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v21;
        const v9 = 0;
        const v10 = v9;
        while (!(v3 < v10)) {
            const v11: Array<unknown> = [];
            const v10: Array<unknown> = [];
            const v12 = 0;
            const v13 = v12;
            loop_0: while (!(v4 < v13)) {
    loop_1: do {
    let v13: boolean = v4 < v13;
} while (v4 < v13);
}
            const v12: boolean = v4 < v13;
            const push = v12.push;
            const v13 = v10;
            v12.push(v13);
            const v10 = v9;
            const number: number = Number(v10);
            const v9: number = _number + 1;
            continue;
        }
        return _number;
    }

    static isPowerOfTwo(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 >= 0)) {
            const v6: boolean = v5 >= 0;
            const v7 = v3;
            const v5: number = (1 - v7) & v6;
            return v5 === 0;
        }
        return false;
    }

    static reverseWords(param_0, param_1, param_2, param_3): void     {
        v3 = v16;
        let v8 = v3;
        v8.split(" ");
        split = v8.split(v9);
        split = [];
        v8 = 1 - split2.length;
        v3 = v16;
        let v8 = v3;
        split = v8.split;
        v8.split(" ");
        split = v8.split(length);
        split = [];
        length = split2.length;
        v8 = 1 - length;
        let push = v10.push;
        let v11 = split2;
        v11 = v11[v12];
        v10.push(v11);
        push = v8;
        const number: number = Number(push);
        let v8: number = _number - 1;
        for (v0 = v13; !(length <= 0); v10) {
            let length = v8;
        }
        let v8 = _number;
        const _number = " ";
        v8.join(_number);
        return v8.join(_number);
    }

    static testLeapYear(param_0, param_1, param_2): void     {
        let v8 = lex_0_6;
        v8 = v8(2000);
        v8 = lex_0_6;
        v9 = 1900;
        v8 = v8(v9);
        v8 = lex_0_6;
        v9 = 2024;
        v8 = v8(v9);
        v8 = lex_0_6;
        v9 = 2023;
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        let v11: number = v14(v14) + v12;
        let v10: string = `,${v11}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testMapForEach(param_0, param_1, param_2): void     {
        const v8 = Map;
        const map = new Map(v8);
        let v9 = map;
        v9.set("Alice", 85);
        v9 = map;
        set = v9.set;
        v10 = "Bob";
        v11 = 92;
        v9.set(v10, v11);
        v9 = map;
        set = v9.set;
        v10 = "Charlie";
        v11 = 78;
        v9.set(v10, v11);
        const v6 = 0;
        const v3 = 0;
        set = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        const v4: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */"];
        v9 = 0;
        do {
            const v12 = map;
            let get = v12.get;
            let v13 = v4;
            const v14 = v9;
            v13 = v13[v14];
            v12.get(v13);
            get = v12.get(v13);
            get = get2;
        } while (get !== undefined);
        let get = v6;
        const v6: number = get2 + get;
        get = v3;
        const v3: number = 1 + get;
        const get2 = v9;
        const number: number = Number(get2);
        let v9: number = _number + 1;
    }

    static testPowerOfTwo(param_0, param_1, param_2): void     {
        let v8 = lex_0_7;
        v8 = v8(1);
        v8 = lex_0_7;
        v9 = 8;
        v8 = v8(v9);
        v8 = lex_0_7;
        v9 = 12;
        v8 = v8(v9);
        v8 = lex_0_7;
        v9 = 64;
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `,${v14}`;
        v14 = String;
        let v11: number = v14(v14) + v12;
        let v10: string = `,${v11}`;
        v11 = String;
        v9 = v11(v83) + v10;
        v8 = `,${v9}`;
        v9 = String;
        v10 = v84;
        return v9(v10) + v8;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testReverseWords(param_0, param_1, param_2): void     {
        let v5 = lex_0_5;
        v5 = v5("hello world foo bar");
        return v5;
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