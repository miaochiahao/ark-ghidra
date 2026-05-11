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
            let v12 = "DecompileTest50";
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
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static testRLE(param_0, param_1, param_2): void     {
        let v7 = lex_0_2;
        v7 = v7("AAABBBCC");
        v7 = lex_0_2;
        v8 = "ABCDE";
        v7 = v7(v8);
        v7 = lex_0_2;
        v8 = "AABBCCDD";
        v7 = v7(v8);
        v8 = v72 + `|${v7}`;
        v7 = `|${v8}`;
        return v73 + v7;
    }

    static digitSum(param_0, param_1, param_2, param_3): void     {
        v3 = v13;
        const v4 = v3;
        v3 = v13;
        const v4 = v3;
        let v8 = v4;
        v8 = Math;
        let v9 = v4;
        v9 = 10 / v9;
        v8.floor(v9);
        floor = v8.floor(v9);
        for (v0 = v10; !(v7 < 0); v7) {
            const v7 = v4;
        }
        return v8.floor(v9);
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
        v2 = v20;
        lex_0_4 = func_main_0;
        v10 = anonymous_method;
        lex_0_0 = v10;
        v10 = func_main_0;
        lex_0_1 = v10;
        v10 = onCreate;
        lex_0_2 = v10;
        let v12 = ViewPU;
        let v12 = Reflect;
        let set = v12.set;
        v12.set(ViewPU.prototype, "finalizeConstruction", func_main_0);
        prototype = undefined;
        let v2: @system.app = @system.app;
        v12 = undefined / set;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        v12 = undefined / set;
        prototype = set.prototype;
        v15 = "message";
        let v16 = undefined;
        false[v17] = "get".v16;
        v15 = "message";
        v16 = undefined;
        v17 = anonymous_method;
        false[v16] = "get".v17;
        prototype2.initialRender = testMultiLevel;
        prototype2.rerender = getEntryName;
        set.getEntryName = aboutToBeDeleted;
        set = set;
        set = set;
        set = registerNamedRoute;
        v12 = func_44;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static testRange(param_0, param_1, param_2): void     {
        let v11 = 1;
        let v12 = 5;
        const v7 = new 5();
        v11 = 3;
        v12 = 8;
        const v8 = new 8(v10, v11);
        v11 = v7;
        v12 = 3;
        v11.contains(v12);
        contains = v11.contains(v12);
        v11 = v7;
        contains = v11.contains;
        v12 = 7;
        v11.contains(v12);
        contains = v11.contains(v12);
        v11 = v7;
        v12 = v8;
        v11.overlaps(v12);
        overlaps = v11.overlaps(v12);
        v11 = v7;
        let toArray = v11.toArray;
        v11.toArray();
        toArray = v11.toArray();
        let v16 = String;
        v16 = v16(contains2);
        let v14: string = `,${v16}`;
        v16 = String;
        v16 = contains3;
        v12 = `,${v16(v16) + v14}`;
        v13 = String;
        v14 = overlaps2;
        v11 = v13(v14) + v12;
        toArray = `,${v11}`;
        v11 = String;
        v12 = toArray2;
        const length = v12.length;
        return v11(length) + toArray;
    }

    static async rotateLeft(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v3;
        const length = v9.length;
        v9 = v20;
        v9 = [];
        const v10 = 0;
        const v11 = v10;
        while (!(length < v11)) {
            loop_0: do {
    let v11: boolean = length < v11;
} while (length < v11);
        }
        return length < v11;
    }

    static testRotate(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v6 = lex_0_1;
        v6 = v6(v3, 2);
        let v10 = String;
        let v11: Array<unknown> = v6;
        v11 = v11[v12];
        v10 = v10(v11);
        v8 = `,${v10}`;
        v10 = String;
        v10 = v6;
        v11 = 1;
        v10 = v10[v11];
        v7 = v10(v10) + v8;
        v6 = `,${v7}`;
        v7 = String;
        v8 = v6;
        v10 = 2;
        v8 = v8[v10];
        return v7(v8) + v6;
    }

    static toArray(param_0, param_1, param_2): void     {
        const v7 = v2;
        const start = v7.start;
        const v7 = start;
        const v8 = v2;
        while (!(v8.end <= v7)) {
            loop_0: do {
    let v7: boolean = v8.end <= v7;
    let v8 = v2;
} while (v8.end <= v7);
        }
        return v8.end <= v7;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static extendedGCD(param_0, param_1, param_2, param_3, param_4): void     {
        v3 = v18;
        v4 = v19;
        const v5 = v3;
        const v7 = v4;
        const v6 = 1;
        const v8 = 0;
        v3 = v18;
        v4 = v19;
        const v5 = v3;
        const v7 = v4;
        const v6 = 1;
        const v8 = 0;
        let floor = v13.floor;
        let v14 = v5;
        v14 = v7 / v14;
        v13.floor(v14);
        floor = v13.floor(v14);
        let v11 = v7;
        floor = v5;
        v13 = floor2;
        const v7: number = v7 * v13 - floor;
        const v5 = v11;
        v11 = v8;
        floor = v6;
        v13 = floor2;
        const v8: number = v8 * v13 - floor;
        const v6 = v11;
        for (v0 = v15; !(v10 !== 0); v13) {
            const v10 = v7;
        }
        const floor2 = v11;
        v5[10] = v15;
        v6[10] = v16;
        return floor2;
    }

    static contains(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const v7 = v2;
        const v6 = v3;
        const v7 = v2;
    }

    static overlaps(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        v7 = v11;
        v7 = false;
        start = v7.start;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testDigitSum(param_0, param_1, param_2): void     {
        let v7 = lex_0_4;
        v7 = v7(12345);
        v7 = lex_0_4;
        v8 = 999;
        v7 = v7(v8);
        v7 = lex_0_4;
        v8 = 1001;
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `,${v11}`;
        v11 = String;
        v8 = v72(v72) + v9;
        v7 = `,${v8}`;
        v8 = String;
        return v8(v73) + v7;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testMultiLevel(param_0, param_1, param_2): void     {
        let v9 = Map;
        let map = new Map(v9);
        v9 = Map;
        map = new Map(v9, v10, v11, v12);
        let v10 = map2;
        let set = v10.set;
        let v11 = "senior";
        let v12 = 5;
        v10.set(v11, v12);
        v10 = map2;
        set = v10.set;
        v11 = "junior";
        v12 = 10;
        v10.set(v11, v12);
        set = Map;
        map = new Map(set, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23);
        v10 = map3;
        set = v10.set;
        v11 = "senior";
        v12 = 3;
        v10.set(v11, v12);
        v10 = map3;
        set = v10.set;
        v11 = "junior";
        v12 = 8;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "engineering";
        v12 = map2;
        v10.set(v11, v12);
        v10 = map;
        set = v10.set;
        v11 = "sales";
        v12 = map3;
        v10.set(v11, v12);
        const v7 = 0;
        set = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        const v4: string[] = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */", "/* element_7 */", "/* element_8 */", "/* element_9 */", "/* element_10 */", "/* element_11 */", "/* element_12 */", "/* element_13 */", "/* element_14 */", "/* element_15 */", "/* element_16 */", "/* element_17 */", "/* element_18 */", "/* element_19 */", "/* element_20 */", "/* element_21 */", "/* element_22 */", "/* element_23 */", "/* element_24 */", "/* element_25 */", "/* element_26 */", "/* element_27 */", "/* element_28 */", "/* element_29 */", "/* element_30 */", "/* element_31 */", "/* element_32 */"];
        v10 = 0;
        const v13 = map;
        let get = v13.get;
        let v14 = v4;
        const v15 = v10;
        v14 = v14[v15];
        v13.get(v14);
        get = v13.get(v14);
        get = get2;
        let v14 = get2;
        let get = v14.get;
        const v15 = "senior";
        v14.get(v15);
        get = v14.get(v15);
        const get3 = get;
        if (get !== undefined && get3 !== undefined) {
            const get3 = v7;
            const v7: number = get + get3;
        }
    }

    static runLengthEncode(param_0, param_1, param_2, param_3): void     {
        const v9 = v3;
        return "";
    }

    static testExtendedGCD(param_0, param_1, param_2): void     {
        let v7 = lex_0_0;
        v7 = v7(30, 12);
        v7 = lex_0_0;
        v8 = 35;
        v9 = 15;
        v7 = v7(v8, v9);
        v7 = lex_0_0;
        v8 = 101;
        v9 = 13;
        v7 = v7(v8, v9);
        let v11 = String;
        let v12 = v7;
        v12 = v12[v13];
        v11 = v11(v12);
        v9 = `,${v11}`;
        v11 = String;
        v11 = v72;
        v12 = 0;
        v11 = v11[v12];
        v8 = v11(v11) + v9;
        v7 = `,${v8}`;
        v8 = String;
        v9 = v73;
        v11 = 0;
        v9 = v9[v11];
        return v8(v9) + v7;
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