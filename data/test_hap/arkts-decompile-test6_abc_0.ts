export class ResourceTable& {
    pkgName@entry;

    static func_main_0(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        if (true) {
            const info = v6.info;
            const v8 = "testTag";
            const v9 = "Succeeded in loading the content.";
            return;
        }
    }

    static func_main_0(param_0, param_1, param_2): void     {
        v0 = v8;
        lex_0_0 = 0;
        let v0: EntryBackupAbility& = EntryBackupAbility&;
        export_0 = v4;
        return;
    }

    static onCreate(param_0, param_1, param_2, param_3, param_4): void     {
        const v13 = JSON;
        stringify = v13.stringify(v6);
    }

    static onDestroy(param_0, param_1, param_2): void     {
        return;
    }

    static onBackground(param_0, param_1, param_2): void     {
        return;
    }

    static onForeground(param_0, param_1, param_2): void     {
        return;
    }

    static onWindowStageCreate(param_0, param_1, param_2, param_3): void     {
        let v8 = "testTag";
        const v6 = v3;
        v8 = func_6;
        return;
    }

    static onWindowStageDestroy(param_0, param_1, param_2): void     {
        return;
    }
}
export class EntryBackupAbility& {
    pkgName@entry;

    constructor(param_0, param_1, param_2, param_3)     {
        throw acc;
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

    constructor(param_0, param_1, param_2)     {
        v2 = v7;
        v2.state = "created";
        return v2;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v7 = v3;
        let v8 = "name";
        const v6 = v7[v8];
        v7 = v13;
        v8 = "name";
        return v7[v8] - v6;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        v7 = Column;
        let v8 = "100%";
        v7 = Column;
        v8 = "100%";
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        const v8 = lex_0_0;
        v7 = Text;
        message = 30;
        v7 = Text;
        message = FontWeight;
        return;
    }

    static async func_main_0(param_0, param_1, param_2): void     { }

    static testRegExp(param_0, param_1, param_2): void     {
        const v6 = RegExp;
        let v7 = "^\\d+$";
        const v3 = new "^\\d+$"(v6);
        const v4 = "12345";
        const v8: string = v4;
        return v3.test(v8);
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        return __message.get();
    }

    static testWeakRef(param_0, param_1, param_2): void     {
        {  }.value = "key";
        v7 = WeakRef;
        v8 = v4;
        v8 = new {  }(v7, v8, v9);
        deref = v8.deref();
        deref = deref;
    }

    static getState(param_0, param_1, param_2): void     {
        const v4 = v2;
        return v4.state;
    }

    static rerender(param_0, param_1, param_2): void     {
        const v5 = v2;
        return;
    }

    static anonymous_method(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
        return;
    }

    static testEnumValue(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(acc === v5)) {
        }
    }

    static testLifecycle(param_0, param_1, param_2): void     {
        const acc: acc = new acc();
        let v9: acc = acc;
        v9 = acc;
        let state = v9.getState;
        state = v9.getState();
        v9 = acc;
        v9 = acc;
        state = v9.getState;
        state = v9.getState();
        v9 = acc;
        v9 = acc;
        state = v9.getState;
        state = v9.getState();
        v9 = state + `|${state}`;
        state = `|${v9}`;
        return state + state;
    }

    static testStringPad(param_0, param_1, param_2): void     {
        let v9 = "42";
        let padStart = v9.padStart;
        padStart = v9.padStart(5, "0");
        v9 = "hi";
        let padEnd = v9.padEnd;
        v10 = 10;
        v11 = ".";
        padEnd = v9.padEnd(v10, v11);
        v9 = "  mid  ";
        let trimStart = v9.trimStart;
        trimStart = v9.trimStart();
        v9 = "  mid  ";
        let trimEnd = v9.trimEnd;
        trimEnd = v9.trimEnd();
        v11 = padEnd + `|${padStart}`;
        v10 = `|${v11}`;
        v9 = trimStart + v10;
        trimEnd = `|${v9}`;
        return trimEnd + trimEnd;
    }

    static distanceTo(param_0, param_1, param_2, param_3): void     {
        let v8 = v2;
        const x = v8.x;
        v8 = v14;
        const v4: number = v8.x - x;
        v8 = v13;
        const y = v8.y;
        v8 = v14;
        const v5: number = v8.y - y;
        v8 = Math;
        let v10: number = v4;
        let v9: number = v4 * v10;
        v9 = v5 * v5 + v9;
        return v8.sqrt(v9);
    }

    static testRegExpExec(param_0, param_1, param_2): void     {
        const v6 = RegExp;
        let v7 = "(\\w+)@(\\w+)\\.(\\w+)";
        v7 = new "(\\w+)@(\\w+)\\.(\\w+)"(v6);
        exec = v7.exec("user@example.com");
        exec = exec;
    }

    static onBackPress(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.state = "back_pressed";
        return true;
    }

    static testArrayBuffer(param_0, param_1, param_2): void     {
        let v6 = ArrayBuffer;
        let v7 = 8;
        const v3 = new 8(v6);
        v6 = DataView;
        v7 = v3;
        v7 = new new 8(v6)(v6, v7, v8, v9);
        let v8 = 0;
        const v9 = 3.14;
        float64 = v4.getFloat64;
        v8 = 0;
        return v4.getFloat64(v8);
    }

    static testSortObjects(param_0, param_1, param_2): void     {
        let v9: Array<unknown> = [];
        const v6: Array<unknown> = [];
        v9 = {  };
        v9[3] = "name";
        v9 = {  };
        v10 = "name";
        v9[1] = v10;
        v9 = {  };
        v10 = "name";
        v9[2] = v10;
        v10 = v6;
        let v11 = v3;
        v10 = v6;
        push = v10.push;
        v11 = v4;
        v10 = v6;
        push = v10.push;
        v11 = v5;
        v10 = v6;
        let sort = v10.sort;
        v11 = func_21;
        sort = v10.sort(v11);
        let v13 = String;
        let v15 = sort;
        const v16 = 0;
        let v14 = v15[v16];
        v15 = "name";
        v14 = v14[v15];
        v13 = v13(v14);
        v11 = `,${v13}`;
        v13 = String;
        v14 = sort;
        v15 = 1;
        v13 = v14[v15];
        v14 = "name";
        v13 = v13[v14];
        v10 = v13(v13) + v11;
        sort = `,${v10}`;
        v10 = String;
        v13 = sort;
        v13 = 2;
        v11 = v13[v13];
        v13 = "name";
        v11 = v11[v13];
        return v10(v11) + sort;
    }

    static testTypeofGuard(param_0, param_1, param_2, param_3): void     { }

    static get getEntryName(): void     {
        return "Index";
    }

    static testMatrixAccess(param_0, param_1, param_2): void     {
        let v7: Array<unknown> = [];
        let v8: string[] = ["/* element_0 */"];
        ["/* element_0 */"][7] = v9;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */"][7] = v10;
        v8 = ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"];
        ["/* element_0 */", "/* element_1 */", "/* element_2 */", "/* element_3 */", "/* element_4 */", "/* element_5 */", "/* element_6 */"][7] = v11;
        v7 = v7;
        v8 = 1;
        v7 = v7[v8];
        v8 = 2;
        return v7[v8];
    }

    static aboutToAppear(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.state = "appeared";
        return;
    }

    static initialRender(param_0, param_1, param_2): void     {
        lex_0_0 = v10;
        let v5 = lex_0_0;
        v5 = lex_0_0;
        observeComponentCreation2 = v5.observeComponentCreation2;
        v6 = func_8;
        v7 = Text;
        v5 = Text;
        v5 = Column;
        pop = v5.pop;
        return;
    }

    static testTypeofBoolean(param_0, param_1, param_2, param_3): void     {
        const v5: string = typeof v3;
        if (!(v5 === "number")) {
            const v5: string = typeof acc;
        }
    }

    static testEnumComparison(param_0, param_1, param_2): void     {
        const v5 = v3;
        return acc === v5;
    }

    static testImmutablePoint(param_0, param_1, param_2): void     {
        let v7 = 0;
        let v8 = 0;
        const v3 = new 0();
        v7 = 3;
        v8 = 4;
        v7 = new 4(v6, v7);
        return v7.distanceTo(v3);
    }

    static updateStateVars(param_0, param_1, param_2, param_3): void     {
        return;
    }

    static aboutToDisappear(param_0, param_1, param_2): void     {
        v2 = v7;
        v2.state = "disappeared";
        return;
    }

    static aboutToBeDeleted(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        let v7 = SubscriberManager;
        get = v7.Get();
        id__ = v10.id__();
        get = v10;
        return;
    }

    static setInitiallyProvidedValue(param_0, param_1, param_2, param_3): void     {
        const v6 = v3;
    }

    static purgeVariableDependenciesOnElmtId(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const __message = v7.__message;
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