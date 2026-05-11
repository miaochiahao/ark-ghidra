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
            let v12 = "DecompileTest36";
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
        let v7 = Column;
        v7.create();
        v7 = Column;
        v7.width("100%");
        v7 = Column;
        v8 = "100%";
        v7.height(v8);
        return;
    }

    static evaluate(param_0, param_1, param_2, param_3, param_4, param_5): void     {
        v5 = v18;
        let v11 = v3;
        v11 = v11 + v17;
        v11 = v11 * v5;
        v11 = v11 - v16;
        const v12 = v4;
        return v11 / (v12 + 1);
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
        v11 = anonymous_method;
        lex_0_2 = v11;
        v11 = func_main_0;
        lex_0_0 = v11;
        v11 = onCreate;
        lex_0_3 = v11;
    }

    static isNumeric(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        const length = v6.length;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static seasonMonth(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
        {
            const v6 = v3;
        }
    }

    static testFlatten(param_0, param_1, param_2): void     {
        let v6: Array<unknown> = [];
        let v7: string[] = ["/* element_0 */"];
        ["/* element_0 */"][6] = v15;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][6] = v16;
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][6] = v17;
        const v4: Array<unknown> = v6;
        v6 = [];
        v7 = 0;
        const v8: number = v7;
        const v9: Array<unknown> = v4;
        const hasLength: boolean = v8 < v9.length;
        const v12 = v8;
        while (!(hasLength < v12.length)) {
            loop_0: do {
    let hasLength: boolean = hasLength < v12.length;
    let v12 = v8;
} while (hasLength < v12.length);
        }
    }

    static testSeasons(param_0, param_1, param_2): void     {
        let v12 = lex_0_0;
        v12 = v12(1);
        let v10: string = `${v12},`;
        v12 = lex_0_0;
        v12 = 4;
        let v9: number = v10 + v12(v12);
        let v8: string = `${v9},`;
        v9 = lex_0_0;
        v10 = 7;
        let v7: number = v8 + v9(v10);
        let v6: string = `${v7},`;
        v7 = lex_0_0;
        v8 = 10;
        let v5: number = v6 + v7(v8);
        const v4: string = `${v5},`;
        v5 = lex_0_0;
        v6 = 13;
        return v4 + v5(v6);
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

    static testEmailCheck(param_0, param_1, param_2): void     {
        let v7 = lex_0_3;
        v7 = v7("user@example.com");
        v7 = lex_0_3;
        v8 = "invalid";
        v7 = v7(v8);
        v7 = lex_0_3;
        v8 = "a@b";
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
    }

    static testEnumLookup(param_0, param_1, param_2): void     {
        {  }[v9] = "NORTH";
        v8 = v6;
        v9 = "1";
        v8[v9] = "EAST";
        v8 = v6;
        v9 = "2";
        v8[v9] = "SOUTH";
        v8 = v6;
        v9 = "3";
        v8[v9] = "WEST";
        v9 = "0";
        const v4 = v8[v9];
        v9 = "2";
        const v5 = v8[v9];
        v9 = Object;
        let keys = v9.keys;
        v9.keys(v6);
        keys = v9.keys(v10);
        v10 = `${v4},`;
        v9 = v10 + v5;
        keys = `${v9},`;
        v9 = String;
        v10 = keys2;
        const length = v10.length;
        return keys + v9(length);
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static simpleEmailCheck(param_0, param_1, param_2, param_3): void     {
        let v8 = v3;
        let indexOf = v8.indexOf;
        v8.indexOf("@");
        indexOf = v8.indexOf(v9);
        indexOf = indexOf2;
        if (!(indexOf < 1)) {
            let v8: boolean = indexOf < 1;
            indexOf = v8.indexOf;
            const v9 = ".";
            const v10 = indexOf2;
            v8.indexOf(v9, v10);
            indexOf = v8.indexOf(v9, v10);
            indexOf = indexOf3;
            v8 = indexOf2;
            if (!(indexOf < v8 + 2)) {
                let indexOf: boolean = indexOf < v8 + 2;
                const v9 = v3;
                const length = v9.length;
                return false;
            }
            return false;
        }
        return false;
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

    static testOrExpressions(param_0, param_1, param_2): void     {
        const v3 = 5;
        const v4 = 10;
        const v5 = 15;
        v10 = v4 === 5;
        if (true) {
            const v7 = true;
        } else {
            const v11 = v4;
        }
        v12 = v5 != [];
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testArithmeticChain(param_0, param_1, param_2): void     {
        let v6 = lex_0_1;
        v6 = v6(2, 3, 4);
        v6 = lex_0_1;
        v7 = 10;
        v8 = 5;
        v9 = 2;
        v6 = v6(v7, v8, v9);
        v8 = String;
        v8 = v8(v6);
        v6 = `${v8},`;
        v8 = String;
        return v6 + v62(v62);
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

    static testStringValidation(param_0, param_1, param_2): void     {
        let v7 = lex_0_2;
        v7 = v7("12345");
        v7 = lex_0_2;
        v8 = "12a45";
        v7 = v7(v8);
        v7 = lex_0_2;
        v8 = "";
        v7 = v7(v8);
        let v11 = String;
        v11 = v11(v7);
        let v9: string = `${v11},`;
        v11 = String;
        v8 = v9 + v72(v72);
        v7 = `${v8},`;
        v8 = String;
        return v7 + v8(v73);
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