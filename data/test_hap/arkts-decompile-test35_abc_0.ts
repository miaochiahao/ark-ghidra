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
            let v12 = "DecompileTest35";
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
        throw isUndefined === "function";
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v3;
        return v6 + v11;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        return v5 > 25;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        const v7 = v3;
        v7.toUpperCase();
        v5 += v7.toUpperCase();
        v5 = v5;
        lex_0_0 = v5;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v6 = v4;
        if (v6 > v3) {
            return v6 > v3;
        }
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
        lex_0_0 = func_main_0;
        v12 = anonymous_method;
        lex_0_1 = v12;
        v12 = func_main_0;
        lex_0_2 = v12;
    }

    static gradeLabel(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 >= 90)) {
            const v5: boolean = v5 >= 90;
            if (!(v5 >= 80)) {
                const v5: boolean = v5 >= 80;
                if (!(v5 >= 70)) {
                    const v5: boolean = v5 >= 70;
                    return "D";
                }
                return "C";
            }
            return "B";
        }
        return "A";
    }

    static testReduce(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v8: Array<unknown> = v3;
        let reduce = v8.reduce;
        v8.reduce(onCreate, 0);
        reduce = v8.reduce(v9, v10);
        reduce = v3.reduce;
        v9 = onWindowStageCreate;
        v10 = v3;
        v10 = v10[v11];
        v3.reduce(v9, v10);
        reduce = v3.reduce(v9, v10);
        v9 = String;
        v10 = reduce2;
        v9 = v9(v10);
        reduce = `${v9},`;
        v9 = String;
        v9 = reduce3;
        return reduce + v9(v9);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static testForEach(param_0, param_1, param_2): void     {
        let v5: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v5 = "";
        lex_0_0 = v5;
        const v6: Array<unknown> = v3;
        v6.forEach(onCreate);
        return;
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

    static reverseString(param_0, param_1, param_2, param_3): void     {
        v3 = v15;
        const v9 = v3;
        let length = v9.length;
        const v7: number = length - 1;
        v3 = v15;
        const v9 = v3;
        length = v9.length;
        const v7: number = length - 1;
        const v10 = v3;
        v10.charAt(v7);
        length = v7;
        const number: number = Number(length);
        const v7: number = _number - 1;
        for (v0 = v12; !(length >= 0); length) {
            let length = v7;
        }
    }

    static testFindIndex(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v7: Array<unknown> = v3;
        let findIndex = v7.findIndex;
        v7.findIndex(onCreate);
        findIndex = v7.findIndex(v8);
        findIndex = String;
        v7 = findIndex2;
        return findIndex(v7);
    }

    static testSplitJoin(param_0, param_1, param_2): void     {
        const v3 = "one,two,three,four";
        let v8: string = v3;
        v8.split(",");
        split = v8.split(v9);
        v8 = split2;
        let join = v8.join;
        v9 = "-";
        v8.join(v9);
        join = v8.join(v9);
        v9 = String;
        v9 = v9(split2.length);
        join = `${v9},`;
        return join + join2;
    }

    static testGradeLabel(param_0, param_1, param_2): void     {
        let v12 = lex_0_0;
        v12 = v12(95);
        let v10: string = `${v12},`;
        v12 = lex_0_0;
        v12 = 85;
        let v9: number = v10 + v12(v12);
        let v8: string = `${v9},`;
        v9 = lex_0_0;
        v10 = 75;
        let v7: number = v8 + v9(v10);
        let v6: string = `${v7},`;
        v7 = lex_0_0;
        v8 = 65;
        let v5: number = v6 + v7(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_0;
        v6 = 50;
        return v4 + v5(v6);
    }

    static testRangeCheck(param_0, param_1, param_2): void     {
        let v8 = lex_0_1;
        v8 = v8(5, 1, 10);
        v8 = lex_0_1;
        v9 = 15;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        v8 = lex_0_1;
        v9 = 1;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        v8 = lex_0_1;
        v9 = 10;
        v10 = 1;
        v11 = 10;
        v8 = v8(v9, v10, v11);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `${v14},`;
        v14 = String;
        v11 = v12 + v82(v82);
        v10 = `${v11},`;
        v11 = String;
        v9 = v10 + v11(v83);
        v8 = `${v9},`;
        v9 = String;
        v10 = v84;
        return v8 + v9(v10);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testNumberChecks(param_0, param_1, param_2): void     {
        let v8 = Number;
        v8.isInteger(42);
        integer = v8.isInteger(v9);
        v8 = Number;
        integer = v8.isInteger;
        v9 = 3.14;
        v8.isInteger(v9);
        integer = v8.isInteger(v9);
        v8 = Number;
        let safeInteger = v8.isSafeInteger;
        v9 = 42;
        v8.isSafeInteger(v9);
        safeInteger = v8.isSafeInteger(v9);
        let v11 = String;
        v11 = v11(integer2);
        v9 = `${v11},`;
        v11 = String;
        v11 = integer3;
        v8 = v9 + v11(v11);
        safeInteger = `${v8},`;
        v8 = String;
        v9 = safeInteger2;
        return safeInteger + v8(v9);
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

    static testStringReverse(param_0, param_1, param_2): void     {
        let v6 = lex_0_2;
        v6 = v6("hello");
        const v4: string = `${v6},`;
        v6 = lex_0_2;
        v6 = "ArkTS";
        return v4 + v6(v6);
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