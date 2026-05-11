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
        v4.parts = [];
        return v8;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return;
    }

    static anonymous_method(param_0, param_1, param_2): void     {
        return new {  }();
    }

    static anonymous_method(param_0, param_1, param_2, param_3, param_4): void     {
        let v9 = v3;
        let charAt = v9.charAt;
        v9.charAt(0);
        charAt = v9.charAt(v10);
        v9 = v15;
        charAt = v9.charAt;
        v10 = 0;
        v9.charAt(v10);
        charAt = v9.charAt(v10);
        charAt = charAt2;
        if (!(charAt3 !== charAt)) {
            let hasCharAt: boolean = charAt3 !== charAt;
            const length = hasCharAt.length;
            hasCharAt = v4;
            return hasCharAt.length - length;
        }
        const length = charAt2;
        const length = 1;
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

    static build(param_0, param_1, param_2): void     {
        let v6 = v2;
        const parts = v6.parts;
        v6 = "";
        parts.join(v6);
        return parts.join(v6);
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
        v0 = v20;
        lex_0_2 = func_main_0;
        v12 = anonymous_method;
        lex_0_1 = v12;
        const v14 = ViewPU;
        const v14 = Reflect;
        let set = v14.set;
        v14.set(ViewPU.prototype, "finalizeConstruction", append);
        prototype = undefined;
        let v0: @system.app = @system.app;
        let isUndefined: boolean = set == undefined;
        prototype = set.prototype;
        set = set;
        set = set;
        prototype = ViewPU;
        isUndefined = set == undefined;
        prototype = set.prototype;
        v17 = "message";
        let v18 = undefined;
        false[v19] = "get".v18;
        v17 = "message";
        v18 = undefined;
        v19 = testCountGrades;
        false[v18] = "get".v19;
        prototype2.initialRender = getEntryName;
        prototype2.rerender = testClassifyScore;
        set.getEntryName = updateStateVars;
        set = set;
        set = set;
        set = registerNamedRoute;
        isUndefined = purgeVariableDependenciesOnElmtId;
        prototype = "";
        prototype2 = {  };
        prototype2 = prototype2;
        return;
    }

    static append(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const parts = v7.parts;
        parts.push(v11);
        return v10;
    }

    static length(param_0, param_1, param_2): void     {
        const v6 = 0;
        const v6 = 0;
        let v10 = v2;
        v10 = v6;
        parts = parts2[v10];
        v7 = v6;
        const number: number = Number(v7);
        const v6: number = _number + 1;
        for (v0 = v11; !(parts.length < v7); v7) {
            let v7 = v6;
            const v9 = v2;
            let parts = v9.parts;
        }
        return _number;
    }

    static prepend(param_0, param_1, param_2, param_3): void     {
        let v7 = v2;
        const parts = v7.parts;
        v7 = 0;
        parts.splice(v7, 0, v13);
        return v12;
    }

    static message(param_0, param_1, param_2): void     {
        const v6 = v2;
        const __message = v6.__message;
        __message.get();
        return __message.get();
    }

    static countGrades(param_0, param_1, param_2, param_3): void     {
        const v4 = 0;
        const v5 = 0;
        const v6 = 0;
        const v10 = 0;
        const v11: number = v10;
        let v12 = v3;
        let v12 = v3;
        const v13 = v10;
        const v11 = v12[v13];
        if (v12 <= 90) {
            let v12 = v4;
            const v4: number = 1 + v12;
        } else {
            let v12: boolean = v12 <= 90;
            if (v12 <= 80) {
                let v12 = v5;
                const v5: number = 1 + v12;
            } else {
                let v12: boolean = v12 <= 80;
                if (v12 <= 70) {
                    let v12 = v6;
                    const v6: number = 1 + v12;
                } else {
                    let v12: boolean = v12 <= 70;
                    const v7: number = 1 + v12;
                }
            }
        }
        const v11 = v10;
        const number: number = Number(v11);
        const v10: number = _number + 1;
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

    static classifyScore(param_0, param_1, param_2, param_3): void     {
        const v5 = v3;
        if (!(v5 <= 90)) {
            const v5: boolean = v5 <= 90;
            if (!(v5 <= 80)) {
                const v5: boolean = v5 <= 80;
                if (!(v5 <= 70)) {
                    const v5: boolean = v5 <= 70;
                }
            }
        }
    }

    static testRegExpExec(param_0, param_1, param_2): void     {
        let v8 = "(\\d+)-(\\d+)";
        const regex = new RegExp();
        const v5 = "abc 123-456 def 789-012";
        v8 = regex;
        let exec = v8.exec;
        v8.exec(v5);
        exec = v8.exec(v9);
        exec = exec2;
        let v10 = exec2;
        let v11 = 0;
        let exec = v10[v11];
        v10 = exec2;
        v11 = 1;
        let v8 = v10[v11];
        v10 = exec2;
        v11 = 2;
        const v9 = v10[v11];
        v11 = v8 + `,${exec}`;
        v10 = `,${v11}`;
        return v9 + v10;
    }

    static testRegExpTest(param_0, param_1, param_2): void     {
        const v4 = "user@example.com";
        const v3 = "not-email";
        let v10 = "^[a-zA-Z]+@[a-zA-Z]+\\.[a-zA-Z]+$";
        v10 = new RegExp();
        let test = v10.test;
        v10.test(v4);
        test = v10.test(v11);
        v10 = regex;
        test = v10.test;
        v11 = v3;
        v10.test(v11);
        test = v10.test(v11);
        v11 = String;
        v11 = v11(test2);
        test = `,${v11}`;
        v11 = String;
        v11 = test3;
        return v11(v11) + test;
    }

    static testSplitLimit(param_0, param_1, param_2): void     {
        const v3 = "a,b,c,d,e,f";
        let v8: string = v3;
        let split = v8.split;
        v8.split(",", 3);
        split = v8.split(v9, v10);
        v8 = v3;
        split = v8.split;
        v9 = ",";
        v10 = 5;
        v8.split(v9, v10);
        split = v8.split(v9, v10);
        let v13 = String;
        v13 = v13(split2.length);
        let v11: string = `:${v13}`;
        v13 = split2;
        v13 = 2;
        v10 = v13[v13] + v11;
        v9 = `|${v10}`;
        v10 = String;
        v11 = split3;
        length = v11.length;
        v8 = v10(length2) + v9;
        split = `:${v8}`;
        v8 = split3;
        v9 = 4;
        return v8[v9] + split;
    }

    static testCountGrades(param_0, param_1, param_2): void     {
        let v5: Array<unknown> = [];
        const v3: Array<unknown> = [];
        v5 = lex_0_1;
        const v6: Array<unknown> = v3;
        return v5(v6);
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

    static testClassifyScore(param_0, param_1, param_2): void     {
        let v10 = lex_0_2;
        v10 = v10(95);
        let v8: string = `,${v10}`;
        v10 = lex_0_2;
        v10 = 85;
        let v7: number = v10(v10) + v8;
        let v6: string = `,${v7}`;
        v7 = lex_0_2;
        v8 = 75;
        let v5: number = v7(v8) + v6;
        const v4: string = `,${v5}`;
        v5 = lex_0_2;
        v6 = 55;
        return v5(v6) + v4;
    }

    static testStringBuilder(param_0, param_1, param_2): void     {
        let acc: acc = new acc();
        const v16: acc = acc;
        let append = v16.append;
        v16.append("Hello");
        append = v16.append(v17);
        append = append2.append;
        append = ", ";
        append2.append(append);
        append = append2.append(append);
        append = append4.append;
        append4.append("World");
        append = append4.append(append3);
        append = append6.append;
        append6.append("!");
        append = append6.append(append5);
        append8.build();
        build = append8.build();
        acc = new acc(append5, append4, append3, append2, append, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32);
        append = acc2.append;
        append5 = "test";
        acc2.append(append5);
        append = acc2.append(append5);
        let length = append7.length;
        append7.length();
        length = append7.length();
        append7 = build2;
        length = `|${append7}`;
        append7 = String;
        const append6 = length2;
        return append7(append6) + length;
    }

    static testSortComparator(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        let v6: Array<unknown> = v3;
        let sort = v6.sort;
        v6.sort(onCreate);
        let v11: Array<unknown> = v3;
        const v12 = 0;
        let v10 = v11[v12];
        let v9: string = `,${v10}`;
        v11 = 1;
        v7 = `,${v3[v11] + v9}`;
        v9 = 2;
        v6 = v3[v9] + v7;
        sort = `,${v6}`;
        v6 = v3;
        v7 = 3;
        return v6[v7] + sort;
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

    static testEntriesIteration(param_0, param_1, param_2): void     {
        const v3: Array<unknown> = [];
        const v4 = "";
        const v7 = 0;
        const v8 = v7;
        const v9 = v3;
        while (!(v9.length < v8)) {
            let v11 = String;
            const v12 = v7;
            v11 = v11(v12);
            const v9: string = `=${v11}`;
            v11 = v3;
            v11 = v7;
            const v8: number = v11[v11] + v9;
            v11 = v4;
            const length = v11.length;
            if (length < 0) {
                const length = v4;
                const v4: string = `;${length}`;
                const length = v4;
                const v4: number = v8 + length;
                const v8 = v7;
                const number: number = Number(v8);
                const v7: number = _number + 1;
                continue;
            }
        }
        return length < 0;
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