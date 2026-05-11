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

    constructor(param_0, param_1, param_2, param_3, param_4)     {
        v2 = v9;
        let v6 = v3;
        super();
        v6 = super();
        v6.label = v11;
        throw v6;
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

    static func_main_0(param_0, param_1, param_2): void     { }

    static double(param_0, param_1, param_2): void     {
        const v5 = v2;
        const value = v5.value;
        return value * 2;
    }

    static double(param_0, param_1, param_2): void     {
        const v6 = v2;
        v6.double();
        double = v6.double();
        return double2 + 1;
    }

    static double(param_0, param_1, param_2): void     {
        const v6 = v2;
        let double = super.double;
        v6.double();
        double = v6.double();
        double = v9;
        return double2 + double.extra;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        v5.updateDirtyElements();
        return;
    }

    static testVoidExpr(param_0, param_1, param_2): void     {
        const v4 = undefined;
        const v6 = String;
        const v7: undefined = v4;
        return v6(v7);
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        __message.set(v11);
        return;
    }

    static testStringPad(param_0, param_1, param_2): void     {
        const v5 = "42";
        let v8: string = v5;
        v8.padStart(6, "0");
        padStart = v8.padStart(v9, v10);
        v8 = v5;
        let padEnd = v8.padEnd;
        v9 = 6;
        v10 = "-";
        v8.padEnd(v9, v10);
        padEnd = v8.padEnd(v9, v10);
        v8 = padStart2;
        padEnd = `${v8}|`;
        return padEnd + padEnd2;
    }

    static getEntryName(param_0, param_1, param_2): void     {
        return "Index";
    }

    static testFinallyBasic(param_0, param_1, param_2): void     {
        throw undefined;
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

    static testArrayFromFill(param_0, param_1, param_2): void     {
        let v7 = 5;
        v7 = new Array();
        let fill = v7.fill;
        v7.fill(7);
        fill = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        v7 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        fill = v7.fill;
        v8 = 0;
        v7.fill(v8, 2, 4);
        let v12 = String;
        let v13: Array = arr;
        v13 = v13[v14];
        v12 = v12(v13);
        v10 = `${v12},`;
        v12 = String;
        v12 = arr;
        v13 = 4;
        v12 = v12[v13];
        v9 = v10 + v12(v12);
        v8 = `${v9},`;
        v9 = String;
        v10 = v4;
        v12 = 2;
        v10 = v10[v12];
        v7 = v8 + v9(v10);
        fill = `${v7},`;
        v7 = String;
        v8 = v4;
        v9 = 3;
        v8 = v8[v9];
        return fill + v7(v8);
    }

    static testLocaleCompare(param_0, param_1, param_2): void     {
        const v3 = "apple";
        const v4 = "banana";
        const v5 = "apple";
        let v11: string = v3;
        let localeCompare = v11.localeCompare;
        v11.localeCompare(v4);
        localeCompare = v11.localeCompare(v12);
        v11 = v3;
        localeCompare = v11.localeCompare;
        v12 = v5;
        v11.localeCompare(v12);
        localeCompare = v11.localeCompare(v12);
        v11 = v4;
        localeCompare = v11.localeCompare;
        v12 = v3;
        v11.localeCompare(v12);
        localeCompare = v11.localeCompare(v12);
        let v14 = String;
        let v15 = localeCompare2;
        v15 = v15 < 0;
        v14 = v14(v15);
        v12 = `${v14},`;
        v14 = String;
        v14 = localeCompare3;
        v14 = v14 === 0;
        v11 = v12 + v14(v14);
        localeCompare = `${v11},`;
        v11 = String;
        v12 = localeCompare4;
        v12 = v12 > 0;
        return localeCompare + v11(v12);
    }

    static testNumberParsing(param_0, param_1, param_2): void     {
        let v8 = parseInt;
        v8 = v8("ff", 16);
        v8 = parseInt;
        v9 = "1010";
        v10 = 2;
        v8 = v8(v9, v10);
        v8 = parseFloat;
        v9 = "3.14";
        v8 = v8(v9);
        v8 = Number;
        v9 = "0";
        v8 = v8(v9);
        let v14 = String;
        v14 = v14(v8);
        let v12: string = `${v14},`;
        v14 = String;
        v10 = `${v12 + v82(v82)},`;
        v11 = String;
        v9 = v10 + v11(v83);
        v8 = `${v9},`;
        v9 = String;
        v10 = v84;
        return v8 + v9(v10);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static testMultiLevelSuper(param_0, param_1, param_2): void     {
        let v7 = 10;
        v7 = new 5();
        let double = v7.double;
        v7.double();
        double = v7.double();
        double = String;
        v7 = double2;
        return double(v7);
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

    static testArrayDestructure(param_0, param_1, param_2): void     {
        let v8: Array<unknown> = [];
        v8 = [];
        let v9 = 0;
        const v4 = v8[v9];
        v8 = v3;
        v9 = 1;
        const v5 = v8[v9];
        v8 = v3;
        v9 = 2;
        const v6 = v8[v9];
        v8 = String;
        v9 = v4 + v5;
        v9 += v6;
        return v8(v9);
    }

    static testConditionalChain(param_0, param_1, param_2): void     {
        const v4 = 15;
        const v6: number = v4;
        if (v6 > 100) {
            const v3 = "huge";
        } else {
            const v6: boolean = v6 > 100;
            if (v6 > 50) {
                const v3 = "big";
            } else {
                const v6: boolean = v6 > 50;
                if (v6 > 10) {
                    const v3 = "medium";
                } else {
                    const v6: boolean = v6 > 10;
                    const v3 = "small";
                }
            }
        }
        return "small";
    }

    static testFinallyWithCatch(param_0, param_1, param_2): void     {
        const v3 = "A";
        let v6 = undefined;
        const v7 = v3;
        const v3: string = `${v7}E`;
        v6 = undefined;
        let v6 = undefined;
    }

    static testObjectDestructure(param_0, param_1, param_2): void     {
        let v9 = 7;
        const v3 = new 7();
        let v7 = v3;
        const x = v7.x;
        v7 = v3;
        const y = v7.y;
        v9 = String;
        v9 = v9(x);
        v7 = `${v9},`;
        v9 = String;
        v9 = y;
        return v7 + v9(v9);
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